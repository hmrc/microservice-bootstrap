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

package uk.gov.hmrc.play.microservice.filters

import akka.stream.Materializer
import akka.util.ByteString
import play.api.Configuration
import play.api.http.HttpEntity.Strict
import play.api.libs.json._
import play.api.mvc.{Filter, RequestHeader, Result}
import play.mvc.Http.MimeTypes

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

abstract class RandomJsonFilter(config: Option[Configuration])(implicit ec: ExecutionContext) extends Filter {
  lazy val isEnabled: Boolean = {
    config match {
      case None => false
      case Some(c) => c.getBoolean("enabled").getOrElse(false)
    }
  }

  implicit def mat: Materializer

  def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    if (isEnabled) {
      val x = nextFilter(requestHeader)
      x.flatMap { result =>
        if (result.header.status == 200) newResponse(result) else Future.successful(result)
      }
    } else {
      nextFilter(requestHeader)
    }
  }

  private def newResponse(result: Result): Future[Result] = {
    result.body.consumeData(mat) map {
      body =>
        val bodyStr = body.decodeString("UTF-8")
        if(bodyStr.isEmpty) {
          result.body
        } else {
          val jsonValue = Json.parse(bodyStr)
          val newJsonValue = process(jsonValue)
          Strict(ByteString(Json.stringify(newJsonValue)), Some(MimeTypes.JSON))
        }
    } map {
      body =>
        Result(result.header, body)
    }
  }

  private def process(jsonValue: JsValue): JsValue = {
    jsonValue match {
      case obj: JsObject =>
        val no = obj.value map {
          case (k, v) => (k, process(v))
        }
        JsObject(no) ++ randomJsonNode
      case arr: JsArray =>
        val na = arr.value map {
          o => process(o)
        }
        JsArray(na)
      case v: JsValue => v
    }
  }

  def randomJsonNode: JsObject = {
    val code = Random.nextInt(25) + 65
    JsObject(List(code.toChar.toString -> JsString("")))
  }
}
