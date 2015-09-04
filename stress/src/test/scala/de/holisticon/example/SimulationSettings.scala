package de.holisticon.example

import io.gatling.core.Predef._
import scala.concurrent.duration._
import scala.language.postfixOps

abstract class SimulationSettings extends Simulation {

  /**
   * Test parameters
   */

  /**
   * Change this uri to the instance you want to test.
   */
  val baseUri = """http://localhost:8080"""

  /** Parallel users */
  val users = 1000
  /** Ramp-up time in seconds*/
  val time = 60 seconds

  /**
   * Test stuff
   */

  val uri = baseUri + """/rest"""

}
