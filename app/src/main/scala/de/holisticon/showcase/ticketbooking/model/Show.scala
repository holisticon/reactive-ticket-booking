package de.holisticon.showcase.ticketbooking.model

/**
 * <p>
 * A show is an instance of an event taking place at a particular venue. A show can have multiple performances.
 * </p>
 *
 * <p>
 * A show contains a set of performances, and a set of ticket prices for each section of the venue for this show.
 * </p>
 *
 * <p>
 * The event and venue form the natural id of this entity, and therefore must be unique.
 * </p>
 *
 * @author Shane Bryzak
 * @author Pete Muir
 */

object Show {
  type Id = Long
}

final case class Show(
  id: Show.Id,
  event: Event,
  venue: Venue,
  performances: Vector[Performance], //ordered by date
  ticketPrices: Set[TicketPrice]
)
