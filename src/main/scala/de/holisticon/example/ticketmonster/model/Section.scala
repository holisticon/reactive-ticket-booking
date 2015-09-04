package de.holisticon.example.ticketmonster.model

/**
 * <p>
 * A section is a specific area within a venue layout. A venue layout may consist of multiple sections.
 * </p>
 *
 * <p>
 * The name and venue form the natural id of this entity, and therefore must be unique.
 * </p>
 *
 * @author Shane Bryzak
 * @author Pete Muir
 */
object Section {
  type Id = Int
}

final case class Section(
    id: Section.Id,
    name: String,
    description: String,
    numberOfRows: Int,
    rowCapacity: Int
) {
  def capacity = numberOfRows * rowCapacity
}
