package de.holisticon.showcase.ticketbooking.model

/**
 * <p>
 * Represents a single venue
 * </p>
 *
 * @author Shane Bryzak
 * @author Pete Muir
 */

object Venue {
  type Id = Int
}

final case class Venue(
    id: Venue.Id,
    name: String,
    address: Address,
    description: String,
    sections: Vector[Section],
    mediaItem: MediaItem
) {
  def capacity = sections.map(_.capacity).sum
}

