package de.holisticon.showcase.ticketbooking.api

import akka.actor.ActorSystem
import akka.cluster.metrics.NodeMetrics
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ HttpResponse, ResponseEntity, StatusCodes }
import akka.http.scaladsl.server.PathMatcher._
import akka.http.scaladsl.server._
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import de.heikoseeberger.akkasse.{ EventStreamMarshalling, ServerSentEvent }
import de.holisticon.showcase.ticketbooking.model._
import de.holisticon.showcase.ticketbooking.rest.{ ModelProtocol, DistributedDataCrudDirectives, NodeMetricsProtocol }
import de.holisticon.showcase.ticketbooking.service.ClusterMetricCollector.EventStream
import de.holisticon.showcase.ticketbooking.service.PerformanceBookingNode.NotEnoughSeats
import de.holisticon.showcase.ticketbooking.service._
import spray.json.JsonPrinter

import scala.concurrent.{ ExecutionContext, Future }
import scala.language.postfixOps

trait HasPrinter {
  implicit def printer: JsonPrinter
}

trait RestApi {
  this: Directives with EventApi with VenueApi with MediaApi with ShowApi with BookingApi with EventCategoryApi with TicketCategoryApi with MetricsApi =>

  val restApi = {
    eventRouting ~ venueRouting ~ mediaRouting ~
      showRouting ~ bookingRouting ~ eventCategoryRouting ~ ticketCategoryApiRouting ~ metricsRouting
  }
}

trait MetricsApi extends SprayJsonSupport with HasPrinter with EventStreamMarshalling {
  this: Directives with ClusterServiceAware with NodeMetricsProtocol =>

  protected implicit def timeout: Timeout
  protected implicit def executionContext: ExecutionContext
  protected implicit def materializer: Materializer

  private def convertMetricEntryToSSE(offset: Long, nm: NodeMetrics): ServerSentEvent = ServerSentEvent(NodeMetricsProtocol.write(nm).compactPrint, None, Some(offset.toString))

  val metricsRouting: Route = {
    (path("metrics" ~ Slash.?) & get) {
      optionalHeaderValueByName("Last-Event-ID") { lastEventIdOpt =>
        complete {
          for {
            fut <- (clusterMetricCollectorRef ? ClusterMetricCollector.Register(lastEventIdOpt.map(_.toLong))).mapTo[EventStream].map(_.stream)
          } yield fut.map((convertMetricEntryToSSE _).tupled)
        }
      }
    }
  }

}

trait EventApi extends SprayJsonSupport with HasPrinter {
  this: Directives with FactDatabaseAware with ModelProtocol with DistributedDataCrudDirectives =>

  protected implicit def timeout: Timeout
  protected implicit def executionContext: ExecutionContext
  protected implicit def materializer: Materializer

  val eventRouting: Route = {
    pathPrefix("events") {
      crud(IntNumber, FactDatabase.eventKey)
    }
  }

}

trait EventCategoryApi extends SprayJsonSupport with HasPrinter {
  this: Directives with FactDatabaseAware with ModelProtocol with DistributedDataCrudDirectives =>

  protected implicit def timeout: Timeout
  protected implicit def executionContext: ExecutionContext
  protected implicit def materializer: Materializer

  val eventCategoryRouting: Route = {
    pathPrefix("eventcategories") {
      crud(Segment, FactDatabase.eventCategoriesKey)
    } ~
      pathPrefix("forge" / "eventcategories") {
        crud(Segment, FactDatabase.eventCategoriesKey)
      }
  }

}

trait TicketCategoryApi {
  this: Directives with FactDatabaseAware with ModelProtocol with DistributedDataCrudDirectives with SprayJsonSupport with HasPrinter =>

  protected implicit def timeout: Timeout
  protected implicit def executionContext: ExecutionContext
  protected implicit def materializer: Materializer

  val ticketCategoryApiRouting: Route = {
    pathPrefix("ticketcategories") {
      crud(IntNumber, FactDatabase.ticketCategoriesKey)
    }
  }

}

trait VenueApi extends ModelProtocol with SprayJsonSupport with HasPrinter {
  this: Directives with DistributedDataCrudDirectives =>

  protected implicit def timeout: Timeout
  protected implicit def executionContext: ExecutionContext
  protected implicit def materializer: Materializer

  val venueRouting: Route = {
    pathPrefix("venues") { crud(IntNumber, FactDatabase.venueKey) }
  }

}

trait ShowApi extends ModelProtocol with SprayJsonSupport with HasPrinter with DistributedDataCrudDirectives {
  this: Directives =>

  protected implicit def timeout: Timeout
  protected implicit def executionContext: ExecutionContext
  protected implicit def materializer: Materializer

  lazy val showRouting: Route = {

    (path("shows" ~ Slash.?) & get) {
      parameters("venue".as[Venue.Id].?, "event".as[Event.Id].?) { (venueId, eventId) =>
        complete((factDatabaseRef ? FactDatabase.GetItems(FactDatabase.showKey))
          .mapTo[Vector[Show]]
          .map(_.filter(show =>
            venueId.getOrElse(show.venue.id) == show.venue.id
              && eventId.getOrElse(show.event.id) == show.event.id)))
      }
    } ~ pathPrefix("shows") {
      crud(LongNumber, FactDatabase.showKey)
    } ~ pathPrefix("shows" / LongNumber / "performance" / IntNumber ~ Slash.?) { (showId, performanceId) =>
      complete((factDatabaseRef ? FactDatabase.GetItems(FactDatabase.showKey))
        .mapTo[Vector[Show]]
        .map { shows =>
          (for {
            show <- shows
            if show.id == showId
            performance <- show.performances
            if performance.id == performanceId
          } yield performance.copy(event = Some(show.event), venue = Some(show.venue))).headOption
        })
    }

  }
}

trait MediaApi extends ModelProtocol with SprayJsonSupport with HasPrinter with DistributedDataCrudDirectives {
  this: Directives with FactDatabaseAware with MediaFetcherAware =>

  protected implicit def executionContext: ExecutionContext
  protected implicit def materializer: Materializer
  protected implicit def timeout: Timeout

  val mediaRouting: Route = {
    pathPrefix("mediaitems") { crud(LongNumber, FactDatabase.mediaItemsKey) } ~
      get {
        path("media" / IntNumber) { id =>

          val res: Future[HttpResponse] = (factDatabaseRef ? FactDatabase.GetItem(FactDatabase.mediaItemsKey, id.toString)).mapTo[Option[MediaItem]].flatMap {
            case Some(mediaItem) =>
              (mediaFetcherRef ? MediaFetcher.Fetch(mediaItem.url)).mapTo[Option[ResponseEntity]]
            case None => Future.successful(None)
          }.map {
            case Some(data) => HttpResponse(status = StatusCodes.OK, entity = data)
            case None => HttpResponse(status = StatusCodes.NotFound)
          }
          complete(res)

        }
      }

  }

}

trait BookingApi {
  this: Directives with ModelProtocol with SprayJsonSupport with HasPrinter with BookingServiceAware =>

  protected implicit val timeout: Timeout
  protected implicit val executionContext: ExecutionContext
  protected implicit val system: ActorSystem
  protected implicit val materializer: Materializer

  val bookingRouting = {
    path("bookings") {
      post {
        entity(as[BookingRequest]) { br: BookingRequest =>
          onSuccess(bookingServiceRef ? br) {
            case b: Booking => complete(b)
            case NotEnoughSeats => complete(HttpResponse(StatusCodes.Conflict, entity = "{\"errors\":[\"Not enough seats\"]}"))
          }
        }
      }
    }
  }

}
