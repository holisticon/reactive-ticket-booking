package de.holisticon.showcase.ticketbooking

import java.nio.file.Files

import akka.cluster.sharding.{ ShardRegion, ClusterShardingSettings, ClusterSharding }
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import akka.stream.{ ActorMaterializer, Materializer }
import com.typesafe.config.{ Config, ConfigFactory }
import de.holisticon.showcase.ticketbooking.api._
import de.holisticon.showcase.ticketbooking.service._
import PerformanceBookingNode.SeatRequest
import PerformanceBookingNode.Init
import kamon.sigar.SigarProvisioner
import org.hyperic.sigar.Sigar
import spray.json.{ CompactPrinter, JsonPrinter }

import scala.language.postfixOps
import akka.actor.{ Props, ActorRef, ActorSystem }
import akka.util.Timeout
import de.holisticon.showcase.ticketbooking.rest._
import de.holisticon.showcase.ticketbooking.service._
import scala.concurrent.duration._

import scala.concurrent.{ Await, ExecutionContext }

object Main {

  def main(args: Array[String]): Unit = {

    val tempDirectory = Files.createTempDirectory("reactive-ticket-booking-sigar").toFile
    tempDirectory.deleteOnExit()
    SigarProvisioner.provision(tempDirectory)
    val sigar = new Sigar()
    ConfigFactory.invalidateCaches()
    new Main(ConfigFactory.load())
  }
}

class Main(val config: Config) extends MediaFetcherAware
    with BookingServiceAware
    with FactDatabaseAware
    with ClusterServiceAware
    with Directives
    with SprayJsonSupport
    with ModelProtocol
    with NodeMetricsProtocol
    with HasPrinter
    with EventApi
    with MediaApi
    with VenueApi
    with ShowApi
    with BookingApi
    with EventCategoryApi
    with TicketCategoryApi
    with MetricsApi
    with RestApi
    with HttpApi {

  override lazy val printer: JsonPrinter = CompactPrinter

  protected implicit val system = ActorSystem.apply(config.getString("booking.cluster-name"), config)
  protected implicit val materializer: Materializer = ActorMaterializer()
  override implicit val timeout: Timeout = Timeout(1 second)
  lazy implicit val executionContext: ExecutionContext = system.dispatcher

  // proper system shutdown on vm termination
  sys.addShutdownHook(Await.result(system.terminate(), 10 seconds))

  override lazy val factDatabaseRef: ActorRef = system.actorOf(Props[FactDatabase](), "FactDatabase")
  override lazy val mediaFetcherRef: ActorRef = system.actorOf(Props[MediaFetcher](new MediaFetcher(materializer)), "MediaFetcher")

  val idExtractor: ShardRegion.ExtractEntityId = {
    case br @ SeatRequest(_, performanceId, _) ⇒ performanceId.toString -> br
    case i @ Init(performanceId, _) ⇒ performanceId.toString -> i
  }

  val shardIdExtractor: ShardRegion.ExtractShardId = {
    case br @ SeatRequest(_, performanceId, _) ⇒ (performanceId.hashCode % 10).toString
    case i @ Init(performanceId, _) ⇒ (performanceId.hashCode % 10).toString
  }

  /** sharding configuration for [[PerformanceBookingNode]]s  **/
  lazy val performanceBookingRegion: ActorRef = ClusterSharding(system).start(
    typeName = "PerformanceBooking",
    entityProps = Props[PerformanceBookingNode](),
    settings = ClusterShardingSettings(system),
    extractEntityId = idExtractor,
    extractShardId = shardIdExtractor
  )

  override lazy val bookingServiceRef: ActorRef = system.actorOf(Props[BookingService](new BookingService(performanceBookingRegion, factDatabaseRef)), "bookingService")

  override val clusterMetricCollectorRef: ActorRef = system.actorOf(Props[ClusterMetricCollector])

  /** Binds the [[HttpApi]] to akka-http */
  Http().bindAndHandle(routes, config.getString("booking.interface"), config.getInt("booking.http.port"))

}
