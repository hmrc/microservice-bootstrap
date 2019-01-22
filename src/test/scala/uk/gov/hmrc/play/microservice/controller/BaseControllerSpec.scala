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

package uk.gov.hmrc.play.microservice.controller

import org.scalatest.{Matchers, WordSpecLike}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Headers
import play.api.test.FakeRequest
import uk.gov.hmrc.play.audit.AuditExtensions


class BaseControllerSpec extends WordSpecLike with Matchers {

  val baseController = new BaseController {}

  "BaseController" should {
    "add path to header carrier for tags" in {
      //this test ensures that the combination of http-verbs-play-25 and play-auditing adds
      //the request path as a tag for classes that extend BaseController
      val request = new FakeRequest[JsValue]("GET", "/the/request/path", Headers(), Json.obj())
      val headerCarrier = baseController.hc(request)
      val tags = new AuditExtensions.AuditHeaderCarrier(headerCarrier).toAuditTags()
      tags.get("path") shouldBe Some("/the/request/path")
    }
  }
}