/*
 * Copyright 2016 HM Revenue & Customs
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

import org.mockito.Mockito
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}
import play.api.mvc.{EssentialFilter, RequestHeader}
import play.api.test.FakeHeaders
import play.api.{PlayException, Application}
import uk.gov.hmrc.play.audit.EventTypes
import uk.gov.hmrc.play.audit.filters.AuditFilter
import uk.gov.hmrc.play.http.NotFoundException
import uk.gov.hmrc.play.http.logging.filters.LoggingFilter
import Mockito._

class DefaultMicroserviceGlobalSpec extends WordSpecLike with Matchers with ScalaFutures with MockitoSugar {

  val requestHeader = mock[RequestHeader]

  when(requestHeader.headers).thenReturn(FakeHeaders(Seq.empty))
  when(requestHeader.method).thenReturn("GET")

  class TestRestGlobal extends DefaultMicroserviceGlobal {
    override val auditConnector = new MockAuditConnector()

    override lazy val appName: String = "testApp"

    override def microserviceMetricsConfig(implicit app: Application) = None

    override def loggingFilter: LoggingFilter = ???

    override def microserviceAuditFilter: AuditFilter = ???

    override def authFilter: Option[EssentialFilter] = ???
  }

  "in a case of an application exception, the framework" should {

    "send an event to DataStream and return 404 status code for a NotFoundException" in {
      val restGlobal = new TestRestGlobal()
      val resultF = restGlobal.onError(requestHeader, new PlayException("", "", new NotFoundException("test"))).futureValue

      resultF.header.status shouldBe 404
      restGlobal.auditConnector.recordedEvent shouldNot be(None)
      restGlobal.auditConnector.recordedEvent.map(_.auditType shouldBe EventTypes.ResourceNotFound)
    }
  }

  "in a case of the microservice endpoint not being found we" should {

    "send ResourceNotFound event to DataStream" in {

      val restGlobal = new TestRestGlobal()
      val resultF = restGlobal.onHandlerNotFound(requestHeader).futureValue

      resultF.header.status shouldBe 404
      restGlobal.auditConnector.recordedEvent shouldNot be(None)
      restGlobal.auditConnector.recordedEvent.map(_.auditType shouldBe EventTypes.ResourceNotFound)

    }
  }

  "in a case of incorrect data being sent to the microservice endpoint we" should {

    "send ServerValidationError event to DataStream" in {

      val restGlobal = new TestRestGlobal()
      val resultF = restGlobal.onBadRequest(requestHeader, "error").futureValue

      resultF.header.status shouldBe 400
      restGlobal.auditConnector.recordedEvent shouldNot be(None)
      restGlobal.auditConnector.recordedEvent.map(_.auditType shouldBe EventTypes.ServerValidationError)

    }
  }

}
