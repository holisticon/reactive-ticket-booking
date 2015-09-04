package de.holisticon.example.ticketmonster.model

/**
 * <p>
 * A ticket represents a seat sold for a particular price.
 * </p>
 *
 * @author Shane Bryzak
 * @author Marius Bogoevici
 * @author Pete Muir
 */
object Ticket {
  type Id = String
}

final case class Ticket(
  id: Ticket.Id,
  ticketCategory: TicketCategory,
  price: Float,
  performance: Performance,
  seat: Seat
)
