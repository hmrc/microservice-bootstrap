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

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}
import play.api.mvc.RequestHeader
import play.api.{PlayException, GlobalSettings}
import uk.gov.hmrc.play.http.{UnauthorizedException, BadRequestException, NotFoundException}

class JsonErrorHandlingSpec extends WordSpecLike with Matchers with ScalaFutures with MockitoSugar {

  val jsh = new GlobalSettings with JsonErrorHandling {}

  val requestHeader = mock[RequestHeader]

  "error handling in onError function" should {

    "convert a NotFoundException to NotFound response" in {
      val resultF = jsh.onError(requestHeader, new PlayException("", "", new NotFoundException("test"))).futureValue
      resultF.header.status shouldBe 404
    }

    "convert a BadRequestException to NotFound response" in {
      val resultF = jsh.onError(requestHeader, new PlayException("", "", new BadRequestException("bad request"))).futureValue
      resultF.header.status shouldBe 400
    }

    "convert an UnauthorizedException to Unauthorized response" in {
      val resultF = jsh.onError(requestHeader, new PlayException("", "", new UnauthorizedException("unauthorized"))).futureValue
      resultF.header.status shouldBe 401
    }

    "convert an Exception to InternalServerError" in {
      val resultF = jsh.onError(requestHeader, new PlayException("", "", new Exception("any application exception"))).futureValue
      resultF.header.status shouldBe 500
    }

  }
}
