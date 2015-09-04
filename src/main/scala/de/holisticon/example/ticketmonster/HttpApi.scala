package de.holisticon.example.ticketmonster

import akka.http.scaladsl.model.{ StatusCodes, HttpResponse }
import akka.http.scaladsl.server.{ ExceptionHandler, Directives }
import akka.pattern.AskTimeoutException
import de.holisticon.example.ticketmonster.rest.RestApi

trait HttpApi {
  this: Directives with RestApi =>

  private val globalExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case e: AskTimeoutException =>
        extractUri { uri =>
          complete(HttpResponse(StatusCodes.ServiceUnavailable, entity = s"Service took to long to answer. Try again later: ${e.getMessage}"))
        }
    }

  def routes =

    handleExceptions(globalExceptionHandler) {
      logRequest("rq") {
        path("") { getFromResource("META-INF/resources/index.html") } ~
          path("admin" ~ Slash.?) { redirect("/admin/app.html", StatusCodes.PermanentRedirect) } ~
          getFromResourceDirectory("META-INF/resources") ~
          pathPrefix("rest") {
            restApi
          }
      }
    }
}

