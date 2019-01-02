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

import java.util.concurrent.TimeUnit

import akka.stream.Materializer
import akka.util.ByteString
import org.mockito.Matchers._
import org.mockito.Mockito.{verify, _}
import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar
import play.api.Configuration
import play.api.http.HttpEntity
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.{RequestHeader, ResponseHeader, Result}

import scala.concurrent._
import scala.concurrent.duration.Duration

class RandomJsonFilterTest extends FunSuite with MockitoSugar {

  // scalastyle:off
  class context {
    val rh: RequestHeader = mock[RequestHeader]
    val r: Result = mock[Result]

    def f(rh: RequestHeader): Future[Result] = {
      Future.successful(r)
    }

    implicit val mat: Materializer = mock[Materializer]
    implicit val ec: ExecutionContextExecutor = ExecutionContext.global

    val configuration: Configuration = mock[Configuration]
    when(configuration.getBoolean("enabled")) thenReturn Some(true)

    val body: HttpEntity = mock[HttpEntity]
    when(r.body) thenReturn body
    when(r.header) thenReturn new ResponseHeader(200, Map.empty)
  }

  class TestRandomJsonFilter(val config: Option[Configuration])(implicit ec: ExecutionContext) extends RandomJsonFilter(config) {
    override def randomJsonNode: JsObject = JsObject(List("Z" -> JsString("")))

    override def mat: Materializer = mock[Materializer]
  }

  test("do nothing when passed empty config") {
    new context {
      private val fresult = new TestRandomJsonFilter(None).apply(f _)(rh)
      assert(await(fresult) === r)
      verify(body, never).consumeData(any())
    }
  }

  test("do nothing when passed a config with config that has enabled set to false") {
    new context {
      when(configuration.getBoolean("enabled")) thenReturn Some(false)
      private val fresult = new TestRandomJsonFilter(Some(configuration)).apply(f _)(rh)
      assert(await(fresult) === r)
      verify(configuration).getBoolean("enabled")
      verify(body, never).consumeData(any())
    }
  }

  test("do nothing when original result status is not 200") {
    new context {
      when(r.header) thenReturn new ResponseHeader(400, Map.empty)
      private val fresult = new TestRandomJsonFilter(Some(configuration)).apply(f _)(rh)
      assert(await(fresult) === r)
      verify(configuration).getBoolean("enabled")
      verify(body, never).consumeData(any())
    }
  }

  test("do nothing when original result is empty body") {
    new context {
      when(body.consumeData(any())) thenReturn Future.successful(ByteString(""))
      when(r.header) thenReturn new ResponseHeader(200, Map.empty)
      private val fresult = new TestRandomJsonFilter(Some(configuration)).apply(f _)(rh)
      private val result = await(fresult)
      assert(resultBody(result.body) === "")
      verify(configuration).getBoolean("enabled")
      verify(body, times(2)).consumeData(any())
    }
  }

  test("return an empty array when original return is empty array") {
    new context {
      when(body.consumeData(any())) thenReturn Future.successful(ByteString("[]"))
      private val fresult = new TestRandomJsonFilter(Some(configuration)).apply(f _)(rh)
      private val result = await(fresult)
      assert(result != r)
      assert(resultBody(result.body) === "[]")
      verify(configuration).getBoolean("enabled")
      verify(body).consumeData(any())
    }
  }

  test("return an object with the random field when original return is empty object") {
    new context {
      when(body.consumeData(any())) thenReturn Future.successful(ByteString("{}"))
      private val fresult = new TestRandomJsonFilter(Some(configuration)).apply(f _)(rh)
      private val result = await(fresult)
      assert(result != r)
      assert(resultBody(result.body) === """{"Z":""}""")
      verify(configuration).getBoolean("enabled")
      verify(body).consumeData(any())
    }
  }

  test("return a string when the original return is a string") {
    new context {
      when(body.consumeData(any())) thenReturn Future.successful(ByteString(""""foo""""))
      private val fresult = new TestRandomJsonFilter(Some(configuration)).apply(f _)(rh)
      private val result = await(fresult)
      assert(result != r)
      assert(resultBody(result.body) === """"foo"""")
      verify(configuration).getBoolean("enabled")
      verify(body).consumeData(any())
    }
  }

  test("return a number when the original return is a number") {
    new context {
      when(body.consumeData(any())) thenReturn Future.successful(ByteString("42"))
      private val fresult = new TestRandomJsonFilter(Some(configuration)).apply(f _)(rh)
      private val result = await(fresult)
      assert(result != r)
      assert(resultBody(result.body) === "42")
      verify(configuration).getBoolean("enabled")
      verify(body).consumeData(any())
    }
  }

  test("return true when the original return is true") {
    new context {
      when(body.consumeData(any())) thenReturn Future.successful(ByteString("true"))
      private val fresult = new TestRandomJsonFilter(Some(configuration)).apply(f _)(rh)
      private val result = await(fresult)
      assert(result != r)
      assert(resultBody(result.body) === "true")
      verify(configuration).getBoolean("enabled")
      verify(body).consumeData(any())
    }
  }

  test("return true when the original return is false") {
    new context {
      when(body.consumeData(any())) thenReturn Future.successful(ByteString("false"))
      private val fresult = new TestRandomJsonFilter(Some(configuration)).apply(f _)(rh)
      private val result = await(fresult)
      assert(result != r)
      assert(resultBody(result.body) === "false")
      verify(configuration).getBoolean("enabled")
      verify(body).consumeData(any())
    }
  }

  test("return an array, whose object has an extra field") {
    new context {
      val resJsonStr ="""[{"a": "1"}]"""
      when(body.consumeData(any())) thenReturn Future.successful(ByteString(resJsonStr))
      private val fresult = new TestRandomJsonFilter(Some(configuration)).apply(f _)(rh)
      private val result = await(fresult)
      assert(result != r)
      assert(Json.parse(resultBody(result.body)) === Json.parse("""[{"a": "1", "Z": ""}]"""))
      verify(configuration).getBoolean("enabled")
      verify(body).consumeData(any())
    }
  }

  test("return an object with an extra field") {
    new context {
      when(body.consumeData(any())) thenReturn Future.successful(ByteString("""{"a":"1"}"""))
      private val fresult = new TestRandomJsonFilter(Some(configuration)).apply(f _)(rh)
      private val result = await(fresult)
      assert(result != r)
      assert(Json.parse(resultBody(result.body)) === Json.parse("""{"a": "1", "Z": ""}"""))
      verify(configuration).getBoolean("enabled")
      verify(body).consumeData(any())
    }
  }

  test("return an object containing embedded object, both with an extra field") {
    new context {
      when(body.consumeData(any())) thenReturn Future.successful(ByteString("""{"a":"1", "b": {"c": "3"}}"""))
      private val fresult = new TestRandomJsonFilter(Some(configuration)).apply(f _)(rh)
      private val result = await(fresult)
      assert(result != r)
      assert(Json.parse(resultBody(result.body)) === Json.parse("""{"a":"1","b":{"c":"3","Z":""},"Z":""}"""))
      verify(configuration).getBoolean("enabled")
      verify(body).consumeData(any())
    }
  }

  private def await[T](awaitable: Awaitable[T]): T =
    Await.result(awaitable, Duration(10, TimeUnit.SECONDS))

  private def resultBody(body: HttpEntity)(implicit ec: ExecutionContext, mat: Materializer): String =
    await(body.consumeData(mat).map(b => b.decodeString("UTF-8")))
}
