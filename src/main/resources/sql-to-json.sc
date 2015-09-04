import java.io.PrintWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import de.holisticon.example.ticketmonster.model._
import scala.collection.mutable
import scala.io.Source
val sql = getClass.getResource("import.sql")
val source = Source.fromFile(sql.toURI,"UTF-8")
val lines = source.getLines()
val mediaItemR = """insert into MediaItem \(mediaType,url\) values \('(.+)','(.+)'\);?""".r
val venueR = """insert into Venue .* values \('([^']+)','([^']+)','(.+)','(.+)','(.+)',(\d+),(\d+)\);?""".r
val sectionR = """insert into Section .* values \('(.+)','(.+)',(\d+),(\d+),(\d+)\);?""".r
val eventCategoryR = """insert into EventCategory .* values \('(.+)'\);?""".r
val ticketCategoryR = """insert into TicketCategory .* values \('(.+)'\);?""".r
val eventR = """insert into Event .* values \('([^']+)','([^']+)',(\d+),(\d+)\);?""".r
val showR = """insert into Show .* values \((\d+),(\d+)\);?""".r
val performanceR = """insert into Performance .* values \((\d+),'([^']+)'\);?""".r
val ticketPriceR = """insert into TicketPrice .* values \((\d+),(\d+),(\d+),([\d\.]+)\);?""".r
val mediaItems = mutable.HashMap[Int, MediaItem]()
val venues = mutable.HashMap[Int, Venue]()
val eventCategories = mutable.HashMap[Int,EventCategory]()
val ticketCategories = mutable.HashSet[TicketCategory]()
val shows = mutable.HashMap[Long,Show]()
val events = mutable.HashMap[Int, Event]()

val jsonObjects = lines.toList
  .filterNot(_.startsWith("--"))
  .filterNot(_.isEmpty)
  .filterNot(_.startsWith("insert into SectionAllocation"))
  .map(_.replace("''", "`"))
  .map{
    case mediaItemR(t, url) =>
      val id = mediaItems.size +1
      mediaItems += id -> MediaItem(id, t, url )
    case venueR(name, city, country, street, description, mid, _) =>
      val id = venues.size + 1
      val mediaItem = mediaItems.getOrElse(mid.toInt, throw new IllegalArgumentException(s"mediaItem $mid not found"))
      venues += id -> Venue(id, name, Address(street, city, country), description, Set.empty[Section], mediaItem)
    case sectionR(name, description, rowCount, rowSize, vid) =>
      val sectionId = venues.values.flatMap(_.sections).size +1
      val oldVenue = venues.getOrElse(vid.toInt, throw new IllegalArgumentException(s"venue for secion $name not found"))
      val section = Section(sectionId, name, description, rowCount.toInt, rowSize.toInt)
      venues += oldVenue.id -> oldVenue.copy(sections = oldVenue.sections+section)
    case eventCategoryR(desc) =>
      eventCategories += eventCategories.size+1 -> EventCategory(desc, desc)
    case ticketCategoryR(desc) =>
      ticketCategories += TicketCategory(ticketCategories.size + 1,desc)
    case eventR(name, description, mid, catId) =>
      val id = events.size+1
      events += id -> Event(id, name, description, mediaItems(mid.toInt), eventCategories(catId.toInt))
    case showR(evid, venid) =>
      val id:Long = shows.size +1
      shows += id -> Show(id, events(evid.toInt), venues(venid.toInt), Vector.empty, Set.empty)
    case performanceR(showId, date) =>
      val show = shows(showId.toInt)
      val performanceId = shows.values.flatMap(_.performances).size + 1
      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
      val parsedDate = LocalDateTime.parse(date, formatter)
      val updatedShow = show.copy(performances = show.performances :+ Performance(performanceId, parsedDate))
      shows += show.id -> updatedShow
    case ticketPriceR(showId, sectionId, ticketCategoryId, price) =>
      val ticketPriceId = shows.values.flatMap(_.ticketPrices).size + 1
      val show = shows(showId.toInt)
      val section = show.venue.sections.find(_.id == sectionId.toInt).head
      val ticketCategory = ticketCategories.find(_.id == ticketCategoryId.toInt).head
      val tp = TicketPrice(ticketPriceId, section, ticketCategory, price.toFloat)
      shows += show.id -> show.copy(ticketPrices = show.ticketPrices + tp)

}


val m = mediaItems.values.toList
val v = venues.values.toList

import spray.json._
import de.holisticon.example.ticketmonster.rest.ModelProtocol._

val jsonEventCategories = eventCategories.values.toList.toJson
val jsonMediaItems = mediaItems.values.toList.toJson
val jsonVenues = venues.values.toList.toJson
val jsonEvents = events.values.toList.toJson
val jsonShows = shows.values.toList.toJson
val jsonTicketCategories = ticketCategories.toList.toJson
val json = JsObject("eventCategories"->jsonEventCategories,
        "mediaItems"->jsonMediaItems,
        "venues" -> jsonVenues,
        "events" -> jsonEvents,
        "shows" -> jsonShows,
        "ticketCategories" -> jsonTicketCategories).prettyPrint
val pw = new PrintWriter("/Users/daniel/projects/reactive-ticket-monster/demo/facts.json") { write(json); close() }

