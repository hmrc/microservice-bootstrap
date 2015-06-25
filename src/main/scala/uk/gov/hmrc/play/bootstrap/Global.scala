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

package uk.gov.hmrc.play.bootstrap

import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import play.api._
import play.api.mvc._
import uk.gov.hmrc.play.audit.filters.AuditFilter
import uk.gov.hmrc.play.audit.http.config.ErrorAuditingSettings
import uk.gov.hmrc.play.config.{AppName, ControllerConfig}
import uk.gov.hmrc.play.filters._
import uk.gov.hmrc.play.graphite.GraphiteConfig
import uk.gov.hmrc.play.http.logging.filters.LoggingFilter

trait MicroserviceFilters {

  def auditFilter:AuditFilter
  def loggingFilter:LoggingFilter

  object MicroserviceLoggingFilter extends LoggingFilter {
    override def controllerNeedsLogging(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsLogging
  }

  object ControllerConfiguration extends ControllerConfig {
    lazy val controllerConfigs = Play.current.configuration.underlying.as[Config]("controllers")
  }

  def defaultFilters: Seq[EssentialFilter] = Seq(
    auditFilter,
    loggingFilter,
    NoCacheFilter,
    RecoveryFilter)
}

abstract class VanillaMicroserviceGlobal
  extends GlobalSettings
  with MicroserviceFilters
  with GraphiteConfig
  with AppName
  with ErrorAuditingSettings
  with JsonErrorHandling {

  override def onStart(app: Application) {
    Logger.info(s"Starting microservice : $appName : in mode : ${app.mode}")
    super.onStart(app)
  }

  override def doFilter(a: EssentialAction): EssentialAction = {
    Filters(super.doFilter(a), defaultFilters: _*)
  }

  // Play 2.0 doesn't support trailing slash: http://play.lighthouseapp.com/projects/82401/tickets/98
  override def onRouteRequest(request: RequestHeader) = super.onRouteRequest(request).orElse {
    Some(request.path).filter(_.endsWith("/")).flatMap(p => super.onRouteRequest(request.copy(path = p.dropRight(1))))
  }
}
