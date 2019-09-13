/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.play.microservice.bootstrap

import play.api.http.Status._
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}
import play.api.{GlobalSettings, Logger}
import uk.gov.hmrc.http.{HttpException, UpstreamErrorResponse}

import scala.concurrent.Future

case class ErrorResponse(
  statusCode: Int,
  message: String,
  xStatusCode: Option[String] = None,
  requested: Option[String]   = None)

trait JsonErrorHandling extends GlobalSettings {

  /**
   * `upstreamWarnStatuses` is used to determine the log level for exceptions
   * relating to a HttpResponse. You can override the Seq to define which
   * response codes should log at a warning level rather an error level.
   *
   * This is used to reduce the number of noise the number of duplicated alerts
   * for a microservice.
   */
  protected val upstreamWarnStatuses: Seq[Int] = Nil
  implicit val erFormats: OFormat[ErrorResponse] = Json.format[ErrorResponse]

  override def onError(request: RequestHeader, ex: Throwable): Future[Result] = {
    val message = s"! Internal server error, for (${request.method}) [${request.uri}] -> "
    val errorResponse = ex match {
      case e: HttpException =>
        logException(e, e.responseCode)
        ErrorResponse(e.responseCode, e.getMessage)
      case e: Exception with UpstreamErrorResponse =>
        logException(e, e.upstreamResponseCode)
        ErrorResponse(e.reportAs, e.getMessage)
      case e: Throwable =>
        Logger.error(message, ex)
        ErrorResponse(INTERNAL_SERVER_ERROR, e.getMessage)
    }

    val result = new Status(errorResponse.statusCode)(Json.toJson(errorResponse))
    Future.successful(result)
  }

  override def onHandlerNotFound(request: RequestHeader): Future[Result] =
    Future.successful {
      val er = ErrorResponse(NOT_FOUND, "URI not found", requested = Some(request.path))
      NotFound(Json.toJson(er))
    }

  override def onBadRequest(request: RequestHeader, error: String): Future[Result] =
    Future.successful {
      val er = ErrorResponse(BAD_REQUEST, error)
      BadRequest(Json.toJson(er))
    }

  private def logException(exception: Exception, responseCode: Int): Unit = {
    if(upstreamWarnStatuses contains responseCode)
      Logger.warn(exception.getMessage, exception)
    else
      Logger.error(exception.getMessage, exception)
  }
}
