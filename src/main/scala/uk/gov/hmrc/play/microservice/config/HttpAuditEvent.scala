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

package uk.gov.hmrc.play.microservice.config

import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.model.DataEvent

trait HttpAuditEvent {

  def appName: String

  object auditDetailKeys {
    val Input = "input"
    val Method = "method"
    val UserAgentString = "userAgentString"
    val Referrer = "referrer"
  }

  object headers {
    val UserAgent = "User-Agent"
    val Referer = "Referer"
  }

  protected[config] def dataEvent(eventType: String, transactionName: String, request: RequestHeader, detail: Map[String, String] = Map())
                                 (implicit hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers)): DataEvent = {

    import auditDetailKeys._
    import headers._
    import uk.gov.hmrc.play.audit.http.HeaderFieldsExtractor._

    val requiredFields = hc.toAuditDetails(Input -> s"Request to ${request.path}",
     Method -> request.method.toUpperCase,
      UserAgentString -> request.headers.get(UserAgent).getOrElse("-"),
      Referrer -> request.headers.get(Referer).getOrElse("-"))

    val tags = hc.toAuditTags(transactionName, request.path)

    DataEvent(appName, eventType, detail = detail ++ requiredFields ++ optionalAuditFieldsSeq(request.headers.toMap), tags = tags)
  }
}

