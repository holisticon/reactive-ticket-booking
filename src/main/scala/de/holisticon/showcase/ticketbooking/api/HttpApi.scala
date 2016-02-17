package de.holisticon.showcase.ticketbooking.api

import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.http.scaladsl.server.{ Directives, ExceptionHandler }
import akka.pattern.AskTimeoutException

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
      path("health") { complete(HttpResponse(status = StatusCodes.NoContent)) } ~
        path("") { getFromResource("META-INF/public/index.html") } ~
        path("admin" ~ Slash.?) { redirect("/admin/app.html", StatusCodes.PermanentRedirect) } ~
        getFromResourceDirectory("META-INF/public") ~
        getFromResourceDirectory("META-INF/resources") ~
        pathPrefix("rest") { restApi }
    }
}

