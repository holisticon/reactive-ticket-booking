package de.holisticon.showcase.ticketbooking.rest

import java.time.format.DateTimeFormatter
import java.time.LocalDateTime

import de.holisticon.showcase.ticketbooking.model._
import spray.json._

trait ModelProtocol extends DefaultJsonProtocol {

  lazy val ISO_LOCAL_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME
  implicit def LocalDateTimeFormat =
    jsonFormat(
      JsonReader.func2Reader((jsv: JsValue) => LocalDateTime.parse(jsv.convertTo[String], ISO_LOCAL_DATE_TIME)),
      JsonWriter.func2Writer((d: LocalDateTime) => JsString(d.format(ISO_LOCAL_DATE_TIME)))
    )

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
