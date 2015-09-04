package de.holisticon.example.ticketmonster.rest

import java.time.{ ZoneOffset, ZoneId, Instant, LocalDateTime }
import java.util.Date

import de.holisticon.example.ticketmonster.model._
import spray.json._

trait ModelProtocol extends DefaultJsonProtocol {

  implicit def DateFormat = jsonFormat(JsonReader.func2Reader(jsv => new Date(jsv.convertTo[Long])), JsonWriter.func2Writer[Date](d => JsNumber(d.getTime)))
  implicit def LocalDateTimeFormat =
    jsonFormat(JsonReader.func2Reader(jsv => LocalDateTime.ofInstant(Instant.ofEpochMilli(jsv.convertTo[Long]), ZoneId.systemDefault())), JsonWriter.func2Writer[LocalDateTime](d => JsNumber(d.toInstant(ZoneOffset.UTC).toEpochMilli)))

  implicit def EventCategoryFormat = jsonFormat2(EventCategory.apply)
  implicit def MediaItemFormat = jsonFormat3(MediaItem.apply)
  implicit def TicketCategoryFormat = jsonFormat2(TicketCategory.apply)
  implicit def SectionFormat = jsonFormat5(Section.apply)
  implicit def TicketPriceFormat = jsonFormat4(TicketPrice.apply)
  implicit def AddressFormat = jsonFormat3(Address.apply)
  implicit def VenueFormat = jsonFormat6(Venue.apply)
  implicit def EventFormat = jsonFormat5(Event.apply)
  implicit def PerformanceFormat = jsonFormat4(Performance.apply)
  implicit def ShowFormat: RootJsonFormat[Show] = rootFormat(lazyFormat(jsonFormat5(Show.apply)))

  implicit def SeatFormat = jsonFormat3(Seat.apply)
  implicit def TicketFormat = jsonFormat5(Ticket.apply)
  implicit def TicketRequestFormat = jsonFormat2(TicketRequest.apply)
  implicit def BookingRequestFormat = jsonFormat4(BookingRequest.apply)
  implicit def BookingFormat = jsonFormat6(Booking.apply)
}

object ModelProtocol extends ModelProtocol
