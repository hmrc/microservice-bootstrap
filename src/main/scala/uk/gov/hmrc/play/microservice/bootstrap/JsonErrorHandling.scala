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
import play.api.libs.json.Json
import play.api.mvc.{RequestHeader, Result}
import play.api.mvc.Results._
import play.api.{GlobalSettings, Logger}
import uk.gov.hmrc.http.{HttpException, Upstream4xxResponse, Upstream5xxResponse}

import scala.concurrent.Future

case class ErrorResponse(
  statusCode: Int,
  message: String,
  xStatusCode: Option[String] = None,
  requested: Option[String]   = None)

trait JsonErrorHandling extends GlobalSettings {

  implicit val erFormats = Json.format[ErrorResponse]

  override def onError(request: RequestHeader, ex: Throwable) = {
    val message = s"! Internal server error, for (${request.method}) [${request.uri}] -> "
    Logger.error(message, ex)
    Future.successful(resolveError(ex))
  }

  private def resolveError(ex: Throwable): Result = {
    val errorResponse = ex match {
      case e: HttpException       => ErrorResponse(e.responseCode, e.getMessage)
      case e: Upstream4xxResponse => ErrorResponse(e.reportAs, e.getMessage)
      case e: Upstream5xxResponse => ErrorResponse(e.reportAs, e.getMessage)
      case e: Throwable           => ErrorResponse(INTERNAL_SERVER_ERROR, e.getMessage)
    }

    new Status(errorResponse.statusCode)(Json.toJson(errorResponse))
  }

  override def onHandlerNotFound(request: RequestHeader) =
    Future.successful {
      val er = ErrorResponse(NOT_FOUND, "URI not found", requested = Some(request.path))
      NotFound(Json.toJson(er))
    }

  override def onBadRequest(request: RequestHeader, error: String) =
    Future.successful {
      val er = ErrorResponse(BAD_REQUEST, error)
      BadRequest(Json.toJson(er))
    }
}
