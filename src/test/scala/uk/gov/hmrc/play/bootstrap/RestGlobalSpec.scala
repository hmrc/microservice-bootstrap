package uk.gov.hmrc.play.bootstrap

import play.api.libs.json.JsValue
import play.api.mvc.{Headers, RequestHeader}
import play.api.test.FakeHeaders
import play.api.{Application, PlayException}
import uk.gov.hmrc.play.audit.EventTypes
import uk.gov.hmrc.play.audit.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.config.AuditingConfig
import uk.gov.hmrc.play.audit.http.connector.{AuditResult, AuditConnector}
import uk.gov.hmrc.play.audit.model.{MergedDataEvent, AuditEvent}
import uk.gov.hmrc.play.http.{HttpResponse, NotFoundException}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.{Future, ExecutionContext}

class RestGlobalSpec extends UnitSpec {

  class TestRestGlobal extends RestGlobal {
    override val auditConnector = new MockAuditConnector()

    override lazy val appName: String = "testApp"

    override def microserviceMetricsConfig(implicit app: Application) = None
  }

  "in a case of an application exception, the framework" should {

    "send an event to DataStream and return 404 status code for a NotFoundException" in {
      val restGlobal = new TestRestGlobal()
      val resultF = restGlobal.onError(new DummyRequestHeader(), new PlayException("", "", new NotFoundException("test")))

      status(resultF) shouldBe 404
      restGlobal.auditConnector.recordedEvent shouldNot be(None)
      restGlobal.auditConnector.recordedEvent.map(_.auditType shouldBe EventTypes.ResourceNotFound)
    }
  }

  "in a case of the microservice endpoint not being found we" should {

    "send ResourceNotFound event to DataStream" in {

      val restGlobal = new TestRestGlobal()
      val resultF = restGlobal.onHandlerNotFound(new DummyRequestHeader())

      status(resultF) shouldBe 404
      restGlobal.auditConnector.recordedEvent shouldNot be(None)
      restGlobal.auditConnector.recordedEvent.map(_.auditType shouldBe EventTypes.ResourceNotFound)

    }
  }

  "in a case of incorrect data being sent to the microservice endpoint we" should {

    "send ServerValidationError event to DataStream" in {

      val restGlobal = new TestRestGlobal()
      val resultF = restGlobal.onBadRequest(new DummyRequestHeader(), "error")

      status(resultF) shouldBe 400
      restGlobal.auditConnector.recordedEvent shouldNot be(None)
      restGlobal.auditConnector.recordedEvent.map(_.auditType shouldBe EventTypes.ServerValidationError)

    }
  }

}

class MockAuditConnector extends AuditConnector {
  var recordedEvent: Option[AuditEvent] = None
  var recordedMergedEvent: Option[MergedDataEvent] = None

  override def sendEvent(event: AuditEvent)(implicit hc: HeaderCarrier, ec : ExecutionContext) = {
    recordedEvent = Some(event)
    Future.successful(AuditResult.Success)
  }

  override def sendMergedEvent(event: MergedDataEvent)(implicit hc: HeaderCarrier, ec : ExecutionContext) = {
    recordedMergedEvent = Some(event)
    Future.successful(AuditResult.Success)
  }

  override protected def logError(s: String, t: Throwable): Unit = ???

  override protected def logError(s: String): Unit = ???

  override protected def callAuditConsumer(url:String, body: JsValue)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[HttpResponse] = ???

  override def auditingConfig: AuditingConfig = ???
}

class DummyRequestHeader extends RequestHeader {

  override def remoteAddress: String = ???

  override def headers: Headers = FakeHeaders(Seq.empty)

  override def queryString: Map[String, Seq[String]] = ???

  override def version: String = ???

  override def method: String = "GET"

  override def path: String = "/"

  override def uri: String = "/"

  override def tags: Map[String, String] = ???

  override def id: Long = ???

  override def secure: Boolean = false
}
