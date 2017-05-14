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

package uk.gov.hmrc.play.microservice.controller

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpecLike}
import play.api.libs.json.{Format, Json}
import play.api.mvc.Results.Ok
import play.api.test.{FakeHeaders, FakeRequest, Helpers}

import scala.concurrent.Future

class BaseControllerSpec extends WordSpecLike with Matchers with ScalaFutures {

  class TestController extends BaseController

  case class Foo(a: Int, b: String)

  implicit val format: Format[Foo] = Json.format[Foo]

  "withJsonBody" should {

    "return OK if request validates successfully" in {
      val controller = new TestController {}
      implicit val fakeRequest = FakeRequest(Helpers.POST, "", FakeHeaders(), Json.parse("""{ "a": 1, "b": "bar" }"""))

      val futureResult = controller.withJsonBody[Foo] { foo =>
        Future.successful(Ok(Json.toJson(foo)))
      }

      whenReady(futureResult) { result =>
        result.header.status shouldBe 200
      }
    }

    "return BadRequest if the request does not validate successfully" in {
      val controller = new TestController {}
      implicit val fakeRequest = FakeRequest(Helpers.POST, "", FakeHeaders(), Json.parse("""{ "a": 1, "b": 1 }"""))

      val futureResult = controller.withJsonBody[Foo] { foo =>
        Future.successful(Ok(Json.toJson(foo)))
      }

      whenReady(futureResult) { result =>
        result.header.status shouldBe 400
      }
    }
  }
}
