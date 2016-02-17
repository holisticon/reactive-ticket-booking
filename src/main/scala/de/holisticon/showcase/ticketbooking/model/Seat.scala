package de.holisticon.showcase.ticketbooking.model

final case class Seat(
  rowNumber: Int,
  number: Int,
  section: Section.Id
)
