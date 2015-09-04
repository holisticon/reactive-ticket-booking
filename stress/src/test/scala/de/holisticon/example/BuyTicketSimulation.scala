package de.holisticon.example

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.language.postfixOps

/**
 * Browses some events, then buys a ticket.
 */
class BuyTicketSimulation extends SimulationSettings {

  val httpProtocol = http
    .baseURL(baseUri)
    .inferHtmlResources()
    .acceptHeader("""image/png,image/*;q=0.8,*/*;q=0.5""")
    .acceptEncodingHeader("""gzip, deflate""")
    .acceptLanguageHeader("""en-US,en;q=0.5""")
    .connection("""keep-alive""")
    .contentTypeHeader("""application/ocsp-request""")
    .userAgentHeader("""Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:33.0) Gecko/20100101 Firefox/33.0""")

  val headers_0 = Map("""Accept""" -> """text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8""")

  val headers_5 = Map(
    """Accept""" -> """application/json, text/javascript, */*; q=0.01""",
    """X-Requested-With""" -> """XMLHttpRequest"""
  )

  val headers_33 = Map(
    """Accept""" -> """application/json, text/javascript, */*; q=0.01""",
    """Cache-Control""" -> """no-cache""",
    """Content-Type""" -> """application/json; charset=UTF-8""",
    """Pragma""" -> """no-cache""",
    """X-Requested-With""" -> """XMLHttpRequest"""
  )

  val scn = scenario("RecordedSimulation")
    .exec(http("/rest/events")
      .get("""/rest/events""")
      .headers(headers_5))
    .pause(1 second, 3 seconds)
    .exec(http("/rest/events/9")
      .get("""/rest/events/9""")
      .headers(headers_5)
      .resources(http("/shows?event=9")
        .get(uri + """/shows?event=9&_=1416586120248""")
        .headers(headers_5)))
    .pause(1 second, 4 seconds)
    .exec(http("/rest/events")
      .get("""/rest/events""")
      .headers(headers_5))
    .pause(2)
    .exec(http("/rest/events/2")
      .get("""/rest/events/2""")
      .headers(headers_5)
      .resources(http("/shows?event=2")
        .get(uri + """/shows?event=2""")
        .headers(headers_5)))
    .pause(2 seconds, 5 seconds)
    .exec(http("/rest/shows/3")
      .get("""/rest/shows/3""")
      .headers(headers_5))
    .pause(3 seconds, 10 seconds)
    .exec(http("/rest/bookings")
      .post("""/rest/bookings""")
      .headers(headers_33)
      .body(StringBody("""{"ticketRequests":[{"ticketPrice":19,"quantity":2},{"ticketPrice":22,"quantity":1}],"email":"hannes@example.org","performance":"6"}"""))
      .resources(http("/shows/3/performance/6")
        .get(uri + """/shows/3/performance/6""")
        .headers(headers_5)))

  setUp(scn.inject(rampUsers(users) over time)).protocols(httpProtocol)
}
