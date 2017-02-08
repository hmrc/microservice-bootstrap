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

import ch.qos.logback.classic.Level
import org.mockito.Matchers.{eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.LoneElement
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.mock.MockitoSugar
import play.api.mvc.RequestHeader
import play.api.{GlobalSettings, Logger, UnexpectedException}
import uk.gov.hmrc.play.http.{BadRequestException, NotFoundException, UnauthorizedException}
import uk.gov.hmrc.play.test.{LogCapturing, UnitSpec}

class JsonErrorHandlingSpec extends UnitSpec with ScalaFutures with MockitoSugar with LogCapturing with LoneElement with Eventually {

  val jsh = new GlobalSettings with JsonErrorHandling {}

  val requestHeader = mock[RequestHeader]

  "error handling in onError function" should {

    "convert a NotFoundException to NotFound response" in {
      val resultF = jsh.onError(requestHeader, new NotFoundException("test")).futureValue
      resultF.header.status shouldBe 404
    }

    "convert a BadRequestException to NotFound response" in {
      val resultF = jsh.onError(requestHeader, new BadRequestException("bad request")).futureValue
      resultF.header.status shouldBe 400
    }

    "convert an UnauthorizedException to Unauthorized response" in {
      val resultF = jsh.onError(requestHeader, new UnauthorizedException("unauthorized")).futureValue
      resultF.header.status shouldBe 401
    }

    "convert an Exception to InternalServerError" in {
      val resultF = jsh.onError(requestHeader, new Exception("any application exception")).futureValue
      resultF.header.status shouldBe 500
    }

    "log an error for a PlayException" in {
      withCaptureOfLoggingFrom(Logger) { logEvents =>
        val method = "some-method"
        val uri = "some-uri"
        when(requestHeader.method).thenReturn(method)
        when(requestHeader.uri).thenReturn(uri)

        jsh.onError(requestHeader, UnexpectedException(Some("any application exception"))).futureValue

        eventually {
          val message = logEvents.filter(_.getLevel == Level.ERROR).loneElement.getMessage
          message should include regex s"! @[a-zA-Z0-9]+ - Internal server error, for \\($method\\) \\[$uri\\] ->".r
        }
      }
    }

    "log an error" in {
      withCaptureOfLoggingFrom(Logger) { logEvents =>
        val method = "some-method"
        val uri = "some-uri"
        when(requestHeader.method).thenReturn(method)
        when(requestHeader.uri).thenReturn(uri)

        jsh.onError(requestHeader, new Exception("any application exception")).futureValue

        eventually {
          logEvents.filter(_.getLevel == Level.ERROR).loneElement.getMessage should include(s"! Internal server error, for ($method) [$uri] ->")
        }
      }
    }
  }
}
