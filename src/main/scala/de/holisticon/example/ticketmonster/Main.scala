package de.holisticon.example.ticketmonster

import java.io.IOException
import java.net.{ InetAddress, ServerSocket }

import akka.cluster.Cluster
import akka.cluster.sharding.{ ShardRegion, ClusterShardingSettings, ClusterSharding }
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import akka.stream.{ ActorMaterializer, Materializer }
import com.typesafe.config.{ Config, ConfigFactory }
import de.holisticon.example.ticketmonster.service.PerformanceBookingNode.SeatRequest
import de.holisticon.example.ticketmonster.service.PerformanceBookingNode.Init
import kamon.sigar.SigarProvisioner
import org.hyperic.sigar.Sigar
import spray.json.{ CompactPrinter, JsonPrinter }

import scala.annotation.tailrec
import scala.language.postfixOps
import akka.actor.{ Address, Props, ActorRef, ActorSystem }
import akka.util.Timeout
import de.holisticon.example.ticketmonster.rest._
import de.holisticon.example.ticketmonster.service._
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext

object Main {

  private def checkPortAvailable(port: Int) = {
    var available = false
    var socket: ServerSocket = null
    try {
      socket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"))
      available = true
    } catch {
      case e: IOException =>
    } finally {
      if (socket != null) socket.close()
    }
    available
  }

  @tailrec
  private def findFreePort(start: Int): Int = {
    if (checkPortAvailable(start)) start
    else findFreePort(start + 1)
  }

  def main(args: Array[String]): Unit = {

    // Extract to default location: ${user.dir}/native
    SigarProvisioner.provision()
    val sigar = new Sigar()

    val freeHttpPort = findFreePort(8080)
    val freeRemotingPort = findFreePort(10337)

    var cfg = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$freeRemotingPort").withFallback(ConfigFactory.load())

    if (freeRemotingPort == 10377) {
      cfg = ConfigFactory.parseString("akka.cluster.seed-nodes = []").withFallback(cfg)
    }

    new Main(cfg).startup(freeHttpPort)

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

  implicit lazy val printer: JsonPrinter = CompactPrinter

  implicit val system = ActorSystem.apply("reactive-ticket-monster", config)
  override lazy val factDatabaseRef: ActorRef = system.actorOf(Props[FactDatabase]())
  protected implicit val materializer: Materializer = ActorMaterializer()
  override lazy val mediaFetcherRef: ActorRef = system.actorOf(Props[MediaFetcher](new MediaFetcher(materializer)))

  val idExtractor: ShardRegion.ExtractEntityId = {
    case br @ SeatRequest(_, performanceId, _) ⇒ performanceId.toString -> br
    case i @ Init(performanceId, _) ⇒ performanceId.toString -> i
  }

  val shardIdExtractor: ShardRegion.ExtractShardId = {
    case br @ SeatRequest(_, performanceId, _) ⇒ (performanceId.hashCode % 10).toString
    case i @ Init(performanceId, _) ⇒ (performanceId.hashCode % 10).toString
  }

  val performanceBookingRegion: ActorRef = ClusterSharding(system).start(
    typeName = "PerformanceBooking",
    entityProps = Props[PerformanceBookingNode](),
    settings = ClusterShardingSettings(system),
    extractEntityId = idExtractor,
    extractShardId = shardIdExtractor
  )

  override lazy val bookingServiceRef: ActorRef = system.actorOf(Props[BookingService](new BookingService(performanceBookingRegion, factDatabaseRef)))

  override implicit val timeout: Timeout = Timeout(1 second)

  lazy implicit val executionContext: ExecutionContext = system.dispatcher

  def startup(port: Int): Unit = {
    val binding = Http().bindAndHandle(routes, "127.0.0.1", port)
    Cluster(system).join(Address("akka.tcp", "reactive-ticket-monster", "127.0.0.1", 10337))
  }

  override val clusterServiceRef: ActorRef = system.actorOf(Props[ClusterService])
}
