package de.holisticon.example

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

import scala.language.postfixOps

/**
 * Browses the site, clicks some buttons. No POSTs, not buying anything.
 */
abstract class ViewSimulation extends SimulationSettings {

  def foo(): Unit

  val httpProtocol = http
    .baseURL(baseUri)
    .inferHtmlResources()
    .acceptHeader("""image/png,image/*;q=0.8,*/*;q=0.5""")
    .acceptEncodingHeader("""gzip, deflate""")
    .acceptLanguageHeader("""en-US,en;q=0.5""")
    .connection("""keep-alive""")
    .userAgentHeader("""Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:33.0) Gecko/20100101 Firefox/33.0""")

  val jsonHeaders = Map(
    """Accept""" -> """application/json, text/javascript, */*; q=0.01""",
    """X-Requested-With""" -> """XMLHttpRequest"""
  )

  val defaultHeaders = Map(
    """Accept""" -> """*/*""",
    """X-Requested-With""" -> """XMLHttpRequest"""
  )

  val browsePage = scenario("BrowseTicketMonsterSimulation")
    .exec(http("/rest/events")
      .get("""/rest/events?_=1416501699675""")
      .headers(jsonHeaders))
    .pause(13)
    // Events
    .exec(http("/rest/venues")
      .get("""/rest/venues?_=1416501699676""")
      .headers(jsonHeaders))
    .pause(9)
  // Venues

  setUp(browsePage.inject(rampUsers(users) over time)).protocols(httpProtocol)
}
