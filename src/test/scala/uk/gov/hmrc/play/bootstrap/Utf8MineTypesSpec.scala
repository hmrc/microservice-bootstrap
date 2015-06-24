package uk.gov.hmrc.play.bootstrap

import play.api.mvc.{Codec, Controller}
import uk.gov.hmrc.play.test.UnitSpec

class Utf8MineTypesSpec extends UnitSpec {

  implicit val codec = Codec.utf_8

  "Controller minetypes" should {

    "have default application json" in {

      val controller = new Controller {}
      val applicationJsonWithUtf8Charset = controller.JSON

      applicationJsonWithUtf8Charset should not be "application/json;charset=utf-8"
    }

    "have application json with utf8 character set" in {

      val controller = new Controller with Utf8MineTypes {}
      val applicationJsonWithUtf8Charset = controller.JSON

      applicationJsonWithUtf8Charset shouldBe "application/json;charset=utf-8"
    }

    "have text html with utf8 character set" in {

      val controller = new Controller with Utf8MineTypes {}
      val textHtmlWithUtf8Charset = controller.HTML

      textHtmlWithUtf8Charset shouldBe "text/html;charset=utf-8"
    }
  }
}
