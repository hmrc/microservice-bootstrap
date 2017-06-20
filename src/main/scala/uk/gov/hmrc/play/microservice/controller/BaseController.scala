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

import play.api.http.MimeTypes
import play.api.libs.json.{JsError, JsSuccess, JsValue, Reads}
import play.api.mvc.{Result, _}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

trait Utf8MimeTypes {
  self: Controller =>

  override val JSON = s"${MimeTypes.JSON};charset=utf-8"

  override def HTML(implicit codec: Codec) = s"${MimeTypes.HTML};charset=utf-8"
}

trait BaseController extends Controller with Utf8MimeTypes {

  implicit def hc(implicit rh: RequestHeader): HeaderCarrier = HeaderCarrier.fromHeadersAndSession(rh.headers)

  protected[controller] def withJsonBody[T](
      f: (T) => Future[Result])(implicit request: Request[JsValue], m: Manifest[T], reads: Reads[T]): Future[Result] =
    request.body.validate[T] match {
      case JsSuccess(payload, _) => f(payload)
      case JsError(errs) =>
        Future.successful(BadRequest(s"Invalid ${m.runtimeClass.getSimpleName} payload: $errs"))
    }

}
