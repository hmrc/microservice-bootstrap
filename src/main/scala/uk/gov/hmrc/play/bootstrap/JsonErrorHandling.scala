package uk.gov.hmrc.play.bootstrap

import scala.concurrent.Future

import play.api.mvc.RequestHeader
import play.api.libs.json.Json
import play.api.GlobalSettings
import play.api.http.Status._
import play.api.mvc.Results._
import uk.gov.hmrc.play.http._

case class ErrorResponse(statusCode: Int, message: String, xStatusCode: Option[String] = None, requested: Option[String] = None)

trait JsonErrorHandling {
  self: GlobalSettings =>

  implicit val erFormats = Json.format[ErrorResponse]

  override def onError(request: RequestHeader, ex: Throwable) = {
    Future.successful {
      val (code, message) = ex.getCause match {
        case e: HttpException => (e.responseCode, e.getMessage)

        case e: Upstream4xxResponse => (e.reportAs, e.getMessage)
        case e: Upstream5xxResponse => (e.reportAs, e.getMessage)

        case e: Throwable => (INTERNAL_SERVER_ERROR, e.getMessage)
      }

      new Status(code)(Json.toJson(ErrorResponse(code, message)))
    }
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful {
      val er = ErrorResponse(NOT_FOUND, "URI not found", requested = Some(request.path))
      NotFound(Json.toJson(er))
    }
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    Future.successful {
      val er = ErrorResponse(BAD_REQUEST, error)
      BadRequest(Json.toJson(er))
    }
  }
}
