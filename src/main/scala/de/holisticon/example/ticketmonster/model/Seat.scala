package de.holisticon.example.ticketmonster.model

final case class Seat(
  rowNumber: Int,
  number: Int,
  section: Section.Id
)
