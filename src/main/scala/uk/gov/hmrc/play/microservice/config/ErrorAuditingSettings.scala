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

package uk.gov.hmrc.play.microservice.config

import play.api.GlobalSettings
import play.api.mvc.{RequestHeader, Result}
import uk.gov.hmrc.http.{HeaderCarrier, JsValidationException, NotFoundException}
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.Future

object EventTypes {

  val RequestReceived: String          = "RequestReceived"
  val TransactionFailureReason: String = "transactionFailureReason"
  val ServerInternalError: String      = "ServerInternalError"
  val ResourceNotFound: String         = "ResourceNotFound"
  val ServerValidationError: String    = "ServerValidationError"
}

trait ErrorAuditingSettings extends GlobalSettings with HttpAuditEvent {
  import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
  import EventTypes._

  def auditConnector: AuditConnector

  private val unexpectedError = "Unexpected error"
  private val notFoundError   = "Resource Endpoint Not Found"
  private val badRequestError = "Request bad format exception"

  override def onError(request: RequestHeader, ex: Throwable): Future[Result] = {
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    val code = ex match {
      case e: NotFoundException           => ResourceNotFound
      case jsError: JsValidationException => ServerValidationError
      case _                              => ServerInternalError
    }

    auditConnector.sendEvent(
      dataEvent(code, unexpectedError, request, Map(TransactionFailureReason -> ex.getMessage))(
        HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))))
    super.onError(request, ex)
  }

  override def onHandlerNotFound(request: RequestHeader): Future[Result] = {
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    auditConnector.sendEvent(
      dataEvent(ResourceNotFound, notFoundError, request)(
        HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))))
    super.onHandlerNotFound(request)
  }

  override def onBadRequest(request: RequestHeader, error: String): Future[Result] = {
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    auditConnector.sendEvent(
      dataEvent(ServerValidationError, badRequestError, request)(
        HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))))
    super.onBadRequest(request, error)
  }
}
