package de.holisticon.example.ticketmonster.rest

import akka.cluster.ddata.LWWMapKey
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model.{ StatusCodes, HttpResponse }
import akka.http.scaladsl.server._
import akka.http.scaladsl.unmarshalling._
import akka.util.Timeout
import de.holisticon.example.ticketmonster.service.FactDatabase.{ PutSuccessful, DeleteFailed, DeleteSuccessful }
import de.holisticon.example.ticketmonster.service.{ FactDatabase, FactDatabaseAware }
import akka.pattern.ask

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag
import scala.util.{ Failure, Success }

trait DistributedDataCrudDirectives extends Directives with FactDatabaseAware {

  protected implicit def timeout: Timeout
  protected implicit def executionContext: ExecutionContext

  def deleteById[K, E](idPathMatcher: PathMatcher1[K], key: LWWMapKey[E]): Route = {
    (delete & path(idPathMatcher ~ Slash.?)) { id =>
      onSuccess(factDatabaseRef ? FactDatabase.DeleteItem(key, id.toString)) {
        case DeleteSuccessful => complete(HttpResponse(StatusCodes.NoContent))
        case DeleteFailed => complete(HttpResponse(StatusCodes.InternalServerError))
      }
    }
  }

  def getAll[E: ClassTag](key: LWWMapKey[E])(implicit marshaller: ToEntityMarshaller[Vector[E]]): Route = {
    get {
      onSuccess(factDatabaseRef ? FactDatabase.GetItems(key)) {
        case items: Vector[_] => complete(items.asInstanceOf[Vector[E]])
      }
    }
  }

  def updateById[K, E: FromEntityUnmarshaller](idPathMatcher: PathMatcher1[K], key: LWWMapKey[E]): Route = {
    (put & path(idPathMatcher ~ Slash.?)) { id =>
      entity(as[E]) { entity: E =>
        onSuccess(factDatabaseRef ? FactDatabase.PutItem(key, id, entity)) {
          case PutSuccessful => complete(HttpResponse(StatusCodes.NoContent))
        }
      }
    }
  }

  def getById[K, E: ClassTag: ToEntityMarshaller](idPathMatcher: PathMatcher1[K], lWWMapKey: LWWMapKey[E]): Route = {
    get {
      path(idPathMatcher ~ Slash.?) { id =>
        onComplete(factDatabaseRef ? FactDatabase.GetItem(lWWMapKey, id.toString)) {
          case Success(Some(result: E)) =>
            complete(result)
          case Success(None) =>
            complete(HttpResponse(StatusCodes.NotFound, entity = s"""{ "errors":["entity $id not found"]}"""))
          case Failure(f) =>
            complete(HttpResponse(StatusCodes.Conflict, entity = s"""{ "errors":["$f"] }")"""))
          case _ =>
            throw new Error
        }
      }
    }
  }

  def createWithoutId[K, E: FromEntityUnmarshaller](key: LWWMapKey[E], id: Any): Route = {
    post {
      entity(as[E]) { entity: E =>

        onSuccess(factDatabaseRef ? FactDatabase.PutItem(key, id, entity)) {
          case PutSuccessful => redirect(id.toString, StatusCodes.Found)
        }
      }
    }
  }

  def crud[K, E: ClassTag: ToEntityMarshaller: FromEntityUnmarshaller](idPathMatcher: PathMatcher1[K], key: LWWMapKey[E])(implicit listMarshaller: ToEntityMarshaller[Vector[E]]): Route = {
    getById(idPathMatcher, key) ~
      (pathEnd | path(Slash.?)) { getAll(key) } ~
      deleteById(idPathMatcher, key) ~
      updateById(idPathMatcher, key)
  }

}
