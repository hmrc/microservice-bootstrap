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

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{WordSpecLike, Matchers}
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSCookie, WSResponse}
import play.api.mvc.{Results, RequestHeader}
import play.api.test.{FakeHeaders, FakeRequest}
import uk.gov.hmrc.play.audit.http.HeaderCarrier
import uk.gov.hmrc.play.auth.controllers.{AuthParamsControllerConfig, AuthConfig, LevelOfAssurance}
import uk.gov.hmrc.play.auth.microservice.connectors._
import _root_.play.api.Routes._
import scala.collection.JavaConversions._


import scala.concurrent.Future
import scala.xml.Elem


class MicroserviceAuthorisationFilterSpec extends WordSpecLike with Matchers with ScalaFutures {

  "MicroserviceAuthorisationFilter.extractAccountAndAccountId with default AuthConfig" should {
    val defaultAuthConfig = AuthConfig(levelOfAssurance = LevelOfAssurance.LOA_2)

    "extract (vat, 99999999) from /vat/99999999" in new SetUp {
      val verb = HttpVerb("GET")
      val resource = ResourceToAuthorise(verb, Regime("vat"), AccountId("99999999"))
      authFilter.extractResource("/vat/99999999", verb, defaultAuthConfig) shouldBe Some(resource)
    }

    "extract (vat, 99999999) from /vat/99999999/calendar" in new SetUp {
      val verb = HttpVerb("GET")
      val resource = ResourceToAuthorise(verb, Regime("vat"), AccountId("99999999"))
      authFilter.extractResource("/vat/99999999/calendar", verb, defaultAuthConfig) shouldBe Some(resource)
    }

    "extract (epaye, 840%2FMODE26A) from /epaye/840%2FMODE26A" in new SetUp {
      val verb = HttpVerb("GET")
      val resource = ResourceToAuthorise(verb, Regime("epaye"), AccountId("840%2FMODE26A"))
      authFilter.extractResource("/epaye/840%2FMODE26A", verb, defaultAuthConfig) shouldBe Some(resource)
    }

    "extract (epaye, 840%2FMODE26A) from /epaye/840%2FMODE26A/account-summary" in new SetUp {
      val verb = HttpVerb("GET")
      val resource = ResourceToAuthorise(verb, Regime("epaye"), AccountId("840%2FMODE26A"))
      authFilter.extractResource("/epaye/840%2FMODE26A/account-summary", verb, defaultAuthConfig) shouldBe Some(resource)
    }

    "extract None from /ping" in new SetUp {
      authFilter.extractResource("ping", HttpVerb("GET"), defaultAuthConfig) shouldBe None
    }
  }

  "MicroserviceAuthorisationFilter.extractResource with AuthConfig configured for government gateway" should {

    "extract (government-gateway-profile/auth/oid, 08732408734) from /profile/auth/oid/08732408734 as special case for government gateway" in new SetUp {
      val verb = HttpVerb("GET")
      val ggAuthConfig = AuthConfig(pattern = "/(profile/auth/oid)/([\\w]+)[/]?".r, servicePrefix = "government-gateway-", levelOfAssurance = LevelOfAssurance.LOA_2)
      val resource = ResourceToAuthorise(verb, Regime("government-gateway-profile/auth/oid"), AccountId("08732408734"))
      authFilter.extractResource("/profile/auth/oid/08732408734", verb, ggAuthConfig) shouldBe Some(resource)
    }
  }

  "MicroserviceAuthorisationFilter.extractResource with AuthConfig configured for anonymous" should {

    "extract (charities, None) from /charities/auth as special case for charities" in new SetUp {
      val verb = HttpVerb("GET")
      val authConfig = AuthConfig(mode = "passcode", levelOfAssurance = LevelOfAssurance.LOA_2)
      val resource = ResourceToAuthorise(verb, Regime("charities"))
      authFilter.extractResource("/charities/blah", verb, authConfig) shouldBe Some(resource)
    }
  }

  "The MicroserviceAuthorisationFilter.apply method when called" should {

    "properly override the account name from controller config and pass the correct account, accountId and delegate authorisation data to the auth connector" in new SetUp {

      val request = FakeRequest("GET", "/anaccount/anid/data", FakeHeaders(), "", tags = Map(ROUTE_VERB -> "GET", ROUTE_CONTROLLER -> "DelegateAuthController"))

      val result = (authFilterWithAccountName.apply((h: RequestHeader) => Future.successful(new Results.Status(200)))(request)).futureValue

      testAuthConnector.capture shouldBe Some(AuthCallCaptured(HttpVerb("GET"), Regime("agent"), Some(AccountId("anid")), AuthRequestParameters(agentRoleRequired = Some("admin"), delegatedAuthRule = Some("lp-paye"), levelOfAssurance = "2")))
    }

    "not override the account name if not specified in the controller config and pass the account from the url and accountId to the auth connector" in new SetUp {

      val request = FakeRequest("GET", "/anaccount/anid/data", FakeHeaders(), "", tags = Map(ROUTE_VERB -> "GET", ROUTE_CONTROLLER -> "DelegateAuthController"))

      val result = authFilter.apply((h: RequestHeader) => Future.successful(new Results.Status(200)))(request).futureValue

      testAuthConnector.capture shouldBe Some(AuthCallCaptured(HttpVerb("GET"), Regime("anaccount"), Some(AccountId("anid")), AuthRequestParameters(agentRoleRequired = Some("admin"), delegatedAuthRule = Some("lp-paye"), levelOfAssurance = "2")))
    }
  }

  class SetUp {

    def buildAuthControllerConfig(includeAccountName: Boolean) = {

      val configMap = Map(
        "DelegateAuthController.authParams.agentRole" -> "admin",
        "DelegateAuthController.authParams.delegatedAuthRule" -> "lp-paye")

      val accountProperty = if (includeAccountName) Map("DelegateAuthController.authParams.account" -> "agent") else Map.empty

      val config = ConfigFactory.parseMap(configMap ++ accountProperty)
      new AuthParamsControllerConfig {
        override def controllerConfigs: Config = config
      }
    }

    case class AuthCallCaptured(method: HttpVerb, account: Regime, accountId: Option[AccountId], authRequestParameters: AuthRequestParameters)

    class TestAuthConnector extends AuthConnector {
      var capture: Option[AuthCallCaptured] = None

      override def authBaseUrl = "authBaseUrl"

      override protected def callAuth(url: String)(implicit hc: HeaderCarrier): Future[WSResponse] = Future.successful(StubWSResponse(200))

      override def authorise(resource: ResourceToAuthorise, authRequestParameters: AuthRequestParameters)(implicit hc: HeaderCarrier): Future[AuthorisationResult] = {
        this.capture = Some(AuthCallCaptured(resource.method, resource.regime, resource.accountId, authRequestParameters))
        Future.successful(AuthorisationResult(true, true))
      }
    }

    case class StubWSResponse(statusCode: Int) extends WSResponse {
      override def allHeaders: Map[String, Seq[String]] = ???
      override def statusText: String = ???
      override def underlying[T]: T = ???
      override def xml: Elem = ???
      override def body: String = ???
      override def header(key: String): Option[String] = ???
      override def cookie(name: String): Option[WSCookie] = ???
      override def cookies: Seq[WSCookie] = ???
      override def status: Int = statusCode
      override def json: JsValue = ???
    }

    val testAuthConnector = new TestAuthConnector

    val authFilter = new MicroserviceAuthorisationFilter {
      override def controllerNeedsAuth(controllerName: String) = true

      override val authParamsConfig = buildAuthControllerConfig(includeAccountName = false)

      override lazy val authConnector = testAuthConnector
    }

    val authFilterWithAccountName = new MicroserviceAuthorisationFilter {
      override def controllerNeedsAuth(controllerName: String) = true

      override val authParamsConfig = buildAuthControllerConfig(includeAccountName = true)

      override lazy val authConnector = testAuthConnector
    }
  }
}
