package uk.gov.hmrc.play.bootstrap

import play.api.mvc._
import play.api.http.MimeTypes
import uk.gov.hmrc.play.audit.http.HeaderCarrier
import scala.concurrent.Future
import play.api.mvc.Result
import play.api.libs.json.{JsError, JsSuccess, Reads, JsValue}
import scala.util.{Failure, Success, Try}

trait Utf8MineTypes {
  self : Controller =>

  override def JSON(implicit codec: Codec) = s"${MimeTypes.JSON};charset=utf-8"

  override def HTML(implicit codec: Codec) = s"${MimeTypes.HTML};charset=utf-8"
}

trait BaseController extends Controller with Utf8MineTypes {

  implicit def hc(implicit rh: RequestHeader) = HeaderCarrier.fromHeadersAndSession(rh.headers)

  protected def withJsonBody[T](f: (T) => Future[Result])(implicit request: Request[JsValue], m: Manifest[T], reads: Reads[T]) =
    Try(request.body.validate[T]) match {
      case Success(JsSuccess(payload, _)) => f(payload)
      case Success(JsError(errs)) => Future.successful(BadRequest(s"Invalid ${m.runtimeClass.getSimpleName} payload: $errs"))
      case Failure(e) => Future.successful(BadRequest(s"could not parse body due to ${e.getMessage}"))
    }
}
