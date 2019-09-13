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

import org.mockito.ArgumentCaptor
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}
import org.scalatestplus.play.OneAppPerSuite
import play.api.Application
import play.api.mvc.{EssentialFilter, RequestHeader, Session}
import play.api.test.FakeHeaders
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.microservice.config.EventTypes
import uk.gov.hmrc.play.microservice.filters.{AuditFilter, LoggingFilter}

import scala.concurrent.ExecutionContext

class DefaultMicroserviceGlobalSpec
    extends WordSpecLike
    with Matchers
    with ScalaFutures
    with MockitoSugar
    with OneAppPerSuite
    with BeforeAndAfterEach {

  val requestHeader: RequestHeader = mock[RequestHeader]

  when(requestHeader.headers).thenReturn(FakeHeaders(Seq.empty))
  when(requestHeader.method).thenReturn("GET")
  when(requestHeader.session).thenReturn(Session())

  class TestRestGlobal extends DefaultMicroserviceGlobal {
    override val auditConnector: AuditConnector = mock[AuditConnector]

    override lazy val appName: String = "testApp"

    override def microserviceMetricsConfig(implicit app: Application) = None

    override def loggingFilter: LoggingFilter = ???

    override def microserviceAuditFilter: AuditFilter = ???

    override def authFilter: Option[EssentialFilter] = ???
  }

  "in a case of an application exception, the framework" should {

    "send an event to DataStream and return 404 status code for a NotFoundException" in {
      val restGlobal = new TestRestGlobal()
      val resultF    = restGlobal.onError(requestHeader, new NotFoundException("test")).futureValue

      val event = verifyAndRetrieveEvent(restGlobal.auditConnector)

      resultF.header.status shouldBe 404
      event.auditType       shouldBe EventTypes.ResourceNotFound
    }
  }

  "in a case of the microservice endpoint not being found we" should {

    "send ResourceNotFound event to DataStream" in {

      val restGlobal = new TestRestGlobal()
      val resultF    = restGlobal.onHandlerNotFound(requestHeader).futureValue

      val event = verifyAndRetrieveEvent(restGlobal.auditConnector)

      resultF.header.status shouldBe 404
      event.auditType       shouldBe EventTypes.ResourceNotFound
    }
  }

  "in a case of incorrect data being sent to the microservice endpoint we" should {

    "send ServerValidationError event to DataStream" in {

      val restGlobal = new TestRestGlobal()
      val resultF    = restGlobal.onBadRequest(requestHeader, "error").futureValue

      val event = verifyAndRetrieveEvent(restGlobal.auditConnector)

      resultF.header.status shouldBe 400
      event.auditType       shouldBe EventTypes.ServerValidationError
    }
  }

  def verifyAndRetrieveEvent(auditConnector: AuditConnector): DataEvent = {
    val captor = ArgumentCaptor.forClass(classOf[DataEvent])
    verify(auditConnector).sendEvent(captor.capture)(any[HeaderCarrier], any[ExecutionContext])

    captor.getValue
  }
}
