package de.holisticon.example.ticketmonster.service

import akka.actor.{ ActorRef, Actor }
import akka.cluster.Cluster
import akka.cluster.ddata._
import akka.cluster.ddata.Replicator._
import akka.util.Timeout
import de.holisticon.example.ticketmonster.model._
import de.holisticon.example.ticketmonster.service.FactDatabase._
import akka.pattern.ask
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.Source
import scala.language.postfixOps

trait FactDatabaseAware {
  def factDatabaseRef: ActorRef
}

class FactDatabase extends Actor {

  implicit val timeout: Timeout = Timeout(10 seconds)
  implicit val system = context.system
  implicit val node = Cluster(system)
  implicit val executionContext = context.dispatcher
  val replicator = DistributedData(system).replicator

  def deleteFromMap[T](key: LWWMapKey[T], id: String): Future[DeleteSuccessful.type] = {
    val response = replicator ? Update(key, LWWMap.empty[T], WriteLocal)(s => s - id)
    response.collect {
      case u @ UpdateSuccess(_, _) => DeleteSuccessful
    }
  }

  def updateMap[T](key: LWWMapKey[T], entry: (String, T)): Future[PutSuccessful.type] = {
    val response = replicator ? Update(key, LWWMap.empty[T], WriteLocal)(s => s + entry)
    response.collect {
      case u @ UpdateSuccess(_, _) => PutSuccessful
    }
  }

  def fetchMap(key: LWWMapKey[_]): Future[LWWMap[_]] = {
    val response = replicator ? Get(key, ReadLocal)
    response.collect {
      case s @ GetSuccess(key: LWWMapKey[_], _) =>
        s.get(key)
      case GetFailure(_, _) =>
        LWWMap.empty[Any]
      case NotFound(_, _) =>
        LWWMap.empty[Any]
    }
  }

  def delete[T](key: LWWMapKey[T], id: Any): Future[DeleteSuccessful.type] = {
    val response = replicator ? Delete(key, WriteLocal)
    response.map {
      case DeleteSuccess(_) | DataDeleted(_) => DeleteSuccessful
    }
  }

  {
    import de.holisticon.example.ticketmonster.rest.ModelProtocol._
    import spray.json._

    val inputString = Source.fromURI(FactDatabase.getClass.getResource("/facts.json").toURI).mkString

    val jsonAst = inputString.parseJson.asJsObject

    jsonAst.fields("eventCategories").convertTo[List[EventCategory]].foreach { category =>
      updateMap(eventCategoriesKey, category.id -> category)
    }

    jsonAst.fields("mediaItems").convertTo[List[MediaItem]].foreach { mediaItem =>
      updateMap(mediaItemsKey, mediaItem.id.toString -> mediaItem)
    }

    jsonAst.fields("venues").convertTo[List[Venue]].foreach { venue =>
      updateMap(venueKey, venue.id.toString -> venue)
    }

    jsonAst.fields("events").convertTo[List[Event]].foreach { event =>
      updateMap(eventKey, event.id.toString -> event)
    }

    jsonAst.fields("shows").convertTo[List[Show]].foreach { show =>
      updateMap(showKey, show.id.toString -> show)
    }

    jsonAst.fields("ticketCategories").convertTo[List[TicketCategory]].foreach { category =>
      updateMap(ticketCategoriesKey, category.id.toString -> category)
    }

  }

  override def receive: Receive = {

    case p @ PutItem(key, id, item) =>
      val originalSender = sender()
      updateMap(key, id.toString -> item).onSuccess {
        case suc => originalSender ! suc
      }

    case DeleteItem(key, id) =>
      val originalSender = sender()
      deleteFromMap(key, id.toString).onSuccess {
        case suc => originalSender ! suc
      }

    case g @ GetItem(key, id) =>
      val originalSender = sender()
      fetchMap(key).onSuccess {
        case map => originalSender ! map.get(id.toString)
      }

    case GetItems(key) =>
      val originalSender = sender()
      fetchMap(key).onSuccess {
        case map => originalSender ! map.entries.values.toVector
      }

  }
}

object FactDatabase {

  val mediaItemsKey = LWWMapKey[MediaItem]("mediaItem")
  val eventCategoriesKey = LWWMapKey[EventCategory]("eventCategory")
  val venueKey = LWWMapKey[Venue]("venue")
  val eventKey = LWWMapKey[Event]("event")
  val ticketCategoriesKey = LWWMapKey[TicketCategory]("ticketCategory")
  val showKey = LWWMapKey[Show]("show")

  sealed trait Protocol

  case class PutItem[T](lWWMapKey: LWWMapKey[T], key: Any, item: T) extends Protocol

  case class DeleteItem(key: LWWMapKey[_], id: Any) extends Protocol

  case class GetItem(key: LWWMapKey[_], id: String) extends Protocol

  case class GetItems(key: LWWMapKey[_]) extends Protocol

  case object PutSuccessful
  case object DeleteSuccessful
  case object DeleteFailed

  object Categories {
    val concert = EventCategory("1", "Concert")
    val theatre = EventCategory("2", "Theatre")
    val musical = EventCategory("3", "Musical")
    val sporting = EventCategory("4", "Sporting")
    val comedy = EventCategory("5", "Comedy")
  }

  object TicketCategories {
    val tcAdult = TicketCategory(1, "Adult")
    val tcChild = TicketCategory(2, "Child 0-14yrs")
  }

}
