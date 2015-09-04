package de.holisticon.showcase.ticketbooking.model

import java.time.LocalDateTime

object Booking {
  type Id = String
}

/**
 * <p>
 * A Booking represents a set of tickets purchased for a performance.
 * </p>
 *
 * <p>
 * Booking's principle members are a <em>set</em> of tickets, and the performance for which the tickets are booked. It also
 * contains meta-data about the booking, including a contact for the booking, a booking date and a cancellation code
 * </p>
 *
 * @author Marius Bogoevici
 */
final case class Booking(
    id: Booking.Id,
    tickets: Set[Ticket],
    performance: Performance,
    cancellationCode: Option[String],
    createdOn: LocalDateTime,
    contactEmail: Option[String]
) {

  /**
   * Compute the total price of all tickets in this booking.
   */
  def getTotalTicketPrice: Float = tickets.map(_.price).sum

}

