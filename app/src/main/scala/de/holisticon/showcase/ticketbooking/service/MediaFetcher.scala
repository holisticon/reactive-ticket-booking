package de.holisticon.showcase.ticketbooking.service

import akka.actor.{ ActorLogging, ActorRef, Actor }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpEntity.Strict
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Location
import akka.stream.Materializer
import akka.util.{ ByteString, Timeout }
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Success

trait MediaFetcherAware {
  def mediaFetcherRef: ActorRef
}

object MediaFetcher {
  case class Fetch(currentUrl: Uri, originalUrl: Uri)
  object Fetch {
    def apply(uri: Uri): Fetch = apply(uri, uri)
  }

  case class AddToCache(id: Uri, responseEntity: Option[Strict])

}

class MediaFetcher(private val materializer: Materializer) extends Actor with ActorLogging {

  import MediaFetcher._
  implicit val executorService = this.context.dispatcher
  implicit val system = this.context.system
  implicit val timeout: Timeout = Timeout(20 seconds)
  implicit val implicitMat = materializer
  private var cache = Map.empty[Uri, Option[Strict]]

  override def receive: Receive = {
    case AddToCache(id, entity @ Some(e)) =>
      log.debug(s"added entity (${e.contentLength} bytes) for uri $id to cache")
      cache += id -> entity
    case AddToCache(id, None) =>
      log.debug(s"cached that $id does not exist")
      cache += id -> None

    case Fetch(_, originalUrl) if cache.contains(originalUrl) =>
      log.debug(s"serving $originalUrl from cache")
      sender() ! cache.get(originalUrl).flatten

    case Fetch(currentUrl, originalUrl) =>
      log.debug(s"fetch for uncached $originalUrl")
      val lastSender = sender()
      val response: Future[HttpResponse] = Http().singleRequest(HttpRequest(method = HttpMethods.GET, currentUrl))
      response.onComplete {
        case Success(r @ HttpResponse(StatusCodes.OK, _, entity, _)) => {
          log.debug(s"received a 200. now loading the bytes for $currentUrl (originally $originalUrl)")
          entity.dataBytes.runFold(ByteString.empty)((akk, next) => akk ++ next).onSuccess {
            case bytes: ByteString =>
              log.debug(s"All bytes there. Now return to the sender and cache it!")
              val strictEntity = Some(Strict(entity.contentType(), bytes))
              self ! AddToCache(originalUrl, strictEntity)
              lastSender ! strictEntity
          }
        }
        case Success(r @ HttpResponse(StatusCodes.NotFound, _, _, _)) =>
          log.debug(s"Resource $currentUrl does not exist. Caching 404 for $originalUrl.")
          self ! AddToCache(originalUrl, None)
          lastSender ! None
        case Success(r @ HttpResponse(sc, headers, _, _)) if sc.isRedirection() => headers.collectFirst {
          case Location(newUri) => newUri
        }.foreach { newUri =>
          log.debug(s"got redirected from $currentUrl to $newUri (originally $originalUrl)")
          self.tell(Fetch(newUri, originalUrl), lastSender)
        }
        case e => throw new RuntimeException(s"unexpected response $e")
      }
  }

}
