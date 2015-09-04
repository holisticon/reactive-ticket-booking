package de.holisticon.example.ticketmonster.model

case object MediaItem {
  type Id = Long
}

/**
 * <p>
 * A reference to a media object such as images, sound bites, video recordings, that can be used in the application.
 * </p>
 *
 * <p>
 * A media item contains the type of the media, which is required to render it correctly, as well as the URL at which the media
 * should be sourced.
 * </p>
 *
 * @author Marius Bogoevici
 * @author Pete Muir
 */
final case class MediaItem(
  id: MediaItem.Id,
  mediaType: String,
  url: String
)
