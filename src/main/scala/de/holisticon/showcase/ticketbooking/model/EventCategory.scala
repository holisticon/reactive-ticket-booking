package de.holisticon.showcase.ticketbooking.model

object EventCategory {
  type Id = String
}

/**
 * <p>
 * Categories of event.
 * </p>
 *
 */
final case class EventCategory(
  id: EventCategory.Id,
  description: String
)
