package de.holisticon.example.ticketmonster.model

object TicketCategory {
  type Id = Int
}

/**
 * <p>
 * Categories of event.
 * </p>
 *
 */
final case class TicketCategory(
  id: TicketCategory.Id,
  description: String
)
