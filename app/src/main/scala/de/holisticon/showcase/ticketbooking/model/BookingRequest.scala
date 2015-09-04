package de.holisticon.showcase.ticketbooking.model

/**
 * {{{
 *   {"ticketRequests": [{"ticketPrice":7,"quantity":1},{"ticketPrice":14,"quantity":1}],"email":"randomdude@random.net","performance":"3" }
 * }}}
 */

final case class BookingRequest(ticketRequests: Vector[TicketRequest], email: String, show: Show.Id, performance: Performance.Id)

final case class TicketRequest(
  ticketPrice: TicketPrice.Id,
  quantity: Int
)

