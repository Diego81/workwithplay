package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation._
import play.api.libs.json._
import play.api.templates.Html

trait RenderMultipleFormats[T] extends Controller {
  
  protected def handleError(formWithErrors: Form[T], html: Html)(implicit request: RequestHeader) = render {
    case Accepts.Html() => BadRequest(html)
    case Accepts.Json() => BadRequest(toJson(formWithErrors))
  }

  protected def handleSuccess(call: Call, flashMessage: String,  properties: Map[String, String] = Map())
  	(implicit request: RequestHeader) = render {
    case Accepts.Html() => Redirect(call).flashing("success" -> flashMessage)
    case Accepts.Json() => Ok(Json.obj("status" -> "Ok", "properties" -> Json.toJson(properties)))
  }

  private def toJson(formWithErrors: Form[T]) = {
    Json.obj(
      "status" -> "Ko",
      "errors" -> formWithErrors.errorsAsJson)
  }
}