/*
 * Copyright 2017 HM Revenue & Customs
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

import play.api.{DefaultGlobal, GlobalSettings, Logger, PlayException}
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.RequestHeader
import play.api.mvc.Results._
import uk.gov.hmrc.play.http.{HttpException, Upstream4xxResponse, Upstream5xxResponse}

import scala.concurrent.Future
import scala.util.control.NonFatal

case class ErrorResponse(statusCode: Int, message: String, xStatusCode: Option[String] = None, requested: Option[String] = None)

trait JsonErrorHandling extends GlobalSettings {

  implicit val erFormats = Json.format[ErrorResponse]

  override def onError(request: RequestHeader, ex: Throwable) = {
    logError(request, ex)
    Future.successful(resolveError(request, ex))
  }

  private def logError(request: RequestHeader, ex: Throwable): Unit = {
    try {
      Logger.error(
        """
          |
          |! %sInternal server error, for (%s) [%s] ->
          | """.stripMargin.format(ex match {
          case p: PlayException => "@" + p.id + " - "
          case _ => ""
        }, request.method, request.uri),
        ex
      )
    } catch {
      case NonFatal(e) => DefaultGlobal.onError(request, e)
    }
  }

  private def resolveError(rh: RequestHeader, ex: Throwable) = {
    val (code, message) = ex match {
      case e: HttpException => (e.responseCode, e.getMessage)
      case e: Upstream4xxResponse => (e.reportAs, e.getMessage)
      case e: Upstream5xxResponse => (e.reportAs, e.getMessage)
      case e: Throwable => (INTERNAL_SERVER_ERROR, e.getMessage)
    }

    new Status(code)(Json.toJson(ErrorResponse(code, message)))
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
