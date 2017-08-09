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

import com.kenshoo.play.metrics.PlayModule
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.EssentialFilter
import uk.gov.hmrc.play.http.logging.filters.LoggingFilter
import play.api.test.Helpers._
import uk.gov.hmrc.play.microservice.filters.AuditFilter

class MicroserviceFiltersSpec extends WordSpecLike with Matchers with MockitoSugar {

  val config: Map[String, _] = Map("play.modules.enabled" -> Seq("com.kenshoo.play.metrics.PlayModule"))

  "MicroserviceFilters" should {

    "include authFilter if defined" in running(new GuiceApplicationBuilder().bindings(new PlayModule).build()) {

      val filters = new MicroserviceFilters {
        override def loggingFilter: LoggingFilter = mock[LoggingFilter]
        override def microserviceAuditFilter: AuditFilter = mock[AuditFilter]
        override def authFilter: Option[EssentialFilter] = Some(mock[EssentialFilter])
      }

      filters.microserviceFilters.size shouldBe 6

    }

    "not include authFilter if not defined" in running(new GuiceApplicationBuilder().bindings(new PlayModule).build()) {

      val filters = new MicroserviceFilters {
        override def loggingFilter: LoggingFilter = mock[LoggingFilter]
        override def microserviceAuditFilter: AuditFilter = mock[AuditFilter]
        override def authFilter: Option[EssentialFilter] = None
      }

      filters.microserviceFilters.size shouldBe 5

    }

  }
}
