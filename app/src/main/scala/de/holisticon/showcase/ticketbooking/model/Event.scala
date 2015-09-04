package de.holisticon.showcase.ticketbooking.model

object Event {
  type Id = Int

}

final case class Event(
  id: Event.Id,
  name: String, // min = 5, max = 50, message = "An event's name must contain between 5 and 50 characters"
  description: String, //@Size(min = 20, max = 1000, message = "An event's description must contain between 20 and 1000 characters")
  mediaItem: MediaItem,
  category: EventCategory
)
