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

package uk.gov.hmrc.play.microservice.bootstrap

import com.kenshoo.play.metrics.MetricsFilter
import play.api._
import play.api.mvc._
import uk.gov.hmrc.play.audit.filters.AuditFilter
import uk.gov.hmrc.play.audit.http.config.ErrorAuditingSettings
import uk.gov.hmrc.play.filters._
import uk.gov.hmrc.play.graphite.GraphiteConfig
import uk.gov.hmrc.play.http.logging.filters.LoggingFilter
import uk.gov.hmrc.play.microservice.bootstrap.Routing.RemovingOfTrailingSlashes
import uk.gov.hmrc.play.microservice.filters.MicroserviceAuthorisationFilter

trait MicroserviceFilters {

  def loggingFilter: LoggingFilter

  def microserviceAuditFilter: AuditFilter

  def metricsFilter: MetricsFilter = MetricsFilter
  
  def authFilter: MicroserviceAuthorisationFilter

  protected lazy val defaultMicroserviceFilters: Seq[EssentialFilter] = Seq(
    metricsFilter,
    microserviceAuditFilter,    
    loggingFilter,
    authFilter,
    NoCacheFilter,
    RecoveryFilter)

  def microserviceFilters: Seq[EssentialFilter] = defaultMicroserviceFilters

}

abstract class DefaultMicroserviceGlobal
  extends GlobalSettings
  with MicroserviceFilters
  with GraphiteConfig
  with RemovingOfTrailingSlashes
  with JsonErrorHandling 
  with ErrorAuditingSettings {

  lazy val appName = Play.current.configuration.getString("appName").getOrElse("APP NAME NOT SET")

  override def onStart(app: Application) {
    Logger.info(s"Starting microservice : $appName : in mode : ${app.mode}")
    super.onStart(app)
  }

  override def doFilter(a: EssentialAction): EssentialAction = {
    Filters(super.doFilter(a), microserviceFilters: _*)
  }

}
