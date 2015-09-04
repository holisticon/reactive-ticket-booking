package de.holisticon.example.ticketmonster.service

import akka.actor.{ Actor, ActorLogging, ReceiveTimeout }
import akka.cluster.sharding.ShardRegion
import akka.persistence.PersistentActor
import de.holisticon.example.ticketmonster.model._
import de.holisticon.example.ticketmonster.service.PerformanceBookingNode._
import scala.concurrent.duration._
import scala.language.postfixOps

object PerformanceBookingNode {
  sealed trait Protocol
  final case class Get(id: Performance.Id) extends Protocol
  case object Stop extends Protocol

  final case class Init(performanceId: Performance.Id, sections: Vector[Section])

  final case class SeatRequest(
    booking: Booking.Id,
    performanceId: Performance.Id,
    sectionsAndQuantities: Map[Section.Id, Int]
  ) extends Protocol

  sealed trait SeatReservationResponse

  final case class SeatReservation(seats: Map[Section.Id, Vector[Seat]]) extends SeatReservationResponse
  case object NotEnoughSeats extends SeatReservationResponse

  private final case class SeatAllocations(seats: Vector[Seat], booking: Booking.Id)

}

class PerformanceBookingNode extends PersistentActor with ActorLogging {

  import ShardRegion.Passivate

  type Row = Int

  private var seats = Map.empty[Seat, Option[Booking.Id]]
  private var bookings = Map.empty[Booking.Id, Vector[Seat]].withDefaultValue(Vector.empty)

  context.setReceiveTimeout(10 minutes)

  override def persistenceId: String = "Performance-" + self.path.name

  def handleSeatRequest(sr: SeatRequest) = {
    val booking = sr.booking
    val sectionAndQuantities = sr.sectionsAndQuantities

    log.info(s"received seatRequest: $sr")

    val requestedSeatsPerSection = sectionAndQuantities.map {
      case (section, quantity) =>
        val freeSeats = seats.collect { case (s @ Seat(_, _, seatSection), None) if seatSection == section => s }.take(quantity).toVector
        (section, freeSeats)
    }

    val enoughSeats = (for {
      (section, quantity) <- sectionAndQuantities
      freeSeats = requestedSeatsPerSection(section)
    } yield freeSeats.size >= quantity).forall(identity)

    val oldSender = sender()
    if (enoughSeats) {
      val allocations = SeatAllocations(requestedSeatsPerSection.values.flatten.toVector, booking)
      persist(allocations) { persistedAllocations =>
        applyAllocation(persistedAllocations)
        oldSender ! SeatReservation(requestedSeatsPerSection)
      }
    } else {
      oldSender ! NotEnoughSeats
    }
  }

  override val receiveCommand: Receive = {
    case init: Init if seats.isEmpty =>
      persist(init) { persistentInit =>
        applyInit(persistentInit)
      }
    case SeatRequest(booking, _, _) if bookings.contains(booking) =>
      // if we already know this booking, return the reserved seats for that booking
      sender() ! SeatReservation(bookings(booking).groupBy(_.section))
    case sr: SeatRequest => handleSeatRequest(sr)
    case ReceiveTimeout ⇒ context.parent ! Passivate(stopMessage = Stop)
    case Stop ⇒ context.stop(self)
  }

  override def receiveRecover: Receive = {
    case init: Init => applyInit(init)
    case evt: SeatAllocations ⇒ applyAllocation(evt)
  }

  def applyInit(init: Init): Unit = {
    seats = {
      for {
        section <- init.sections
        row <- 1 to section.numberOfRows
        seat <- 1 to section.rowCapacity
      } yield Seat(row, seat, section.id) -> None
    }.toMap
  }

  private def applyAllocation(allocations: SeatAllocations): Unit = {
    allocations.seats.foreach { seat: Seat =>
      seats += seat -> Some(allocations.booking)
    }
    bookings += allocations.booking -> allocations.seats
  }

}
