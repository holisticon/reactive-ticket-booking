package de.holisticon.example.ticketmonster.model

import java.time.LocalDateTime

object Performance {
  type Id = Int
}

/**
 * <p>
 * A performance represents a single instance of a show.
 * </p>
 *
 * <p>
 * The show and date form the natural id of this entity, and therefore must be unique. JPA requires us to use the class level
 * <code>@Table</code> constraint.
 * </p>
 *
 * @author Marius Bogoevici
 * @author Pete Muir
 */
final case class Performance(
  id: Performance.Id,
  date: LocalDateTime,
  event: Option[Event] = None,
  venue: Option[Venue] = None
)
