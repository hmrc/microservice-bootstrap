package uk.gov.hmrc.play.bootstrap

import play.api.{GlobalSettings, PlayException}
import uk.gov.hmrc.play.bootstrap.JsonErrorHandling
import uk.gov.hmrc.play.http.{BadRequestException, NotFoundException, UnauthorizedException}
import uk.gov.hmrc.play.test.UnitSpec

class JsonErrorHandlingSpec extends UnitSpec {

  val jsh = new GlobalSettings with JsonErrorHandling {}

  "error handling in onError function" should {

    "convert a NotFoundException to NotFound response" in {
      val resultF = jsh.onError(new DummyRequestHeader(), new PlayException("", "", new NotFoundException("test")))
      status(resultF) shouldBe 404
    }

    "convert a BadRequestException to NotFound response" in {
      val resultF = jsh.onError(new DummyRequestHeader(), new PlayException("", "", new BadRequestException("bad request")))
      status(resultF) shouldBe 400
    }

    "convert an UnauthorizedException to Unauthorized response" in {
      val resultF = jsh.onError(new DummyRequestHeader(), new PlayException("", "", new UnauthorizedException("unauthorized")))
      status(resultF) shouldBe 401
    }

    "convert an Exception to InternalServerError" in {
      val resultF = jsh.onError(new DummyRequestHeader(), new PlayException("", "", new Exception("any application exception")))
      status(resultF) shouldBe 500
    }

  }
}
