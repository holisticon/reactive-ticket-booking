package de.holisticon.example.ticketmonster.service

import java.util.Date

import akka.pattern.ask
import akka.actor.{ Actor, ActorRef }
import akka.util.Timeout
import de.holisticon.example.ticketmonster.service.PerformanceBookingNode.{ SeatReservationResponse, NotEnoughSeats, SeatReservation, SeatRequest }
import scala.concurrent.duration._
import de.holisticon.example.ticketmonster.model._
import scala.language.postfixOps

trait BookingServiceAware {
  def bookingServiceRef: ActorRef
}

class BookingService(performanceBookingRegion: ActorRef, factDatabaseRef: ActorRef) extends Actor {

  implicit val executorService = this.context.dispatcher
  implicit val system = this.context.system
  implicit val timeout: Timeout = Timeout(10 seconds)

  override def receive: Receive = {

    case br @ BookingRequest(ticketRequests, email, showId, performanceId) =>
      val originalSender = sender()
      val showResult = (factDatabaseRef ? FactDatabase.GetItem(FactDatabase.showKey, showId.toString)).mapTo[Option[Show]]
      showResult.onSuccess {
        case Some(show: Show) =>
          val performance = show.performances.find(_.id == performanceId).get

          val sectionRequests = for {
            ticketRequest <- ticketRequests
            ticketPrice <- show.ticketPrices
            if ticketPrice.id == ticketRequest.ticketPrice
          } yield (ticketPrice, ticketRequest.quantity)

          val bookingId = s"$showId-$performanceId-$email-${ticketRequests.mkString("-")}"

          val seatRequest = SeatRequest(booking = bookingId, performanceId = performanceId, sectionRequests.map { case (a, b) => (a.section.id, b) }.toMap)

          performanceBookingRegion ! PerformanceBookingNode.Init(performanceId, show.venue.sections)
          (performanceBookingRegion ? seatRequest).mapTo[SeatReservationResponse].onSuccess {

            case seatReservation: SeatReservation =>
              val tickets = for {
                (ticketPrice, amount) <- sectionRequests
                seat <- seatReservation.seats(ticketPrice.section.id)
              } yield Ticket(id = s"$performanceId-${seat.section}-${seat.rowNumber}-${seat.number}", ticketCategory = ticketPrice.ticketCategory, price = ticketPrice.price, performance, seat)
              val booking = Booking(
                id = bookingId,
                tickets = tickets.toSet,
                performance = performance,
                cancellationCode = None,
                createdOn = new Date(),
                contactEmail = Some(email)
              )
              originalSender ! booking

            case NotEnoughSeats => originalSender ! NotEnoughSeats
          }

      }

  }

}
