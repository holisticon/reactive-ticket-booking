package de.holisticon.example.ticketmonster.model

/**
 * <p>
 * Contains price categories - each category represents the price for a ticket in a particular section at a particular venue for
 * a particular event, for a particular ticket category.
 * </p>
 *
 * <p>
 * The section, show and ticket category form the natural id of this entity, and therefore must be unique. JPA requires us to use the class level
 * <code>@Table</code> constraint
 * </p>
 *
 * @author Shane Bryzak
 * @author Pete Muir
 */
object TicketPrice {
  type Id = Long
}

final case class TicketPrice(
  id: TicketPrice.Id,
  section: Section,
  ticketCategory: TicketCategory,
  price: Float
)
