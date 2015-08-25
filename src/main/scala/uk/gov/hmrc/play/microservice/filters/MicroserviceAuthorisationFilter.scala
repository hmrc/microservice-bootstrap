/*
 * Copyright 2015 HM Revenue & Customs
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

import play.api.Routes
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.auth.controllers.{AuthConfig, AuthParamsControllerConfig}
import uk.gov.hmrc.play.auth.microservice.filters.AuthorisationFilter

trait MicroserviceAuthorisationFilter extends AuthorisationFilter {

  def controllerNeedsAuth(controllerName: String): Boolean
  def authParamsConfig: AuthParamsControllerConfig

  /**
   * @return None if authorisation is not required OR the RequestHeader does not give enough information for us
   *         to tell if we need authorisation or not
   */
  override def authConfig(rh: RequestHeader): Option[AuthConfig] =
    rh.tags.get(Routes.ROUTE_CONTROLLER).flatMap { name =>
      if (controllerNeedsAuth(name)) Some(authParamsConfig.authConfig(name))
      else None
    }

}
