package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation._
import play.api.libs.json._
import anorm._
import models.User
import scala.collection.mutable.HashMap
import play.api.templates.Html

object Users extends Controller {

  val form = Form(
    mapping(
      "id" -> ignored(NotAssigned: Pk[Int]),
      "username" -> nonEmptyText,
      "email" -> email,
      "age" -> number)(User.apply)(User.unapply))

  def add = Action {
    Ok(views.html.users.add(form))
  }

  def save = Action { implicit request =>
    form.bindFromRequest.fold(
      formWithErrors => handleError(formWithErrors, views.html.users.add(formWithErrors)),
      user => {
        User.save(user)
        handleSuccess("User successfully created!")
      })
  }
  
  private def handleError(formWithErrors: Form[User], html: Html)(implicit request: RequestHeader) = render {
          case Accepts.Html() => BadRequest(html)
          case Accepts.Json() => BadRequest(toJson(formWithErrors))
        }

  private def handleSuccess(flashMessage: String)(implicit request: RequestHeader) = render {
          case Accepts.Html() => Redirect(routes.Users.list).flashing("success" -> flashMessage)
          case Accepts.Json() => Ok(Json.obj("status" -> "Ok"))
        }
  
  private def toJson(formWithErrors: Form[User]) = {
    Json.obj(
      "status" -> "Ko",
      "errors" -> formWithErrors.errorsAsJson)
  }

  def list = Action { implicit request =>
    render {
      case Accepts.Html() => Ok(views.html.users.list(User.list))
      case Accepts.Json() => Ok(toJsonHighLevel(User.list))
    }
  }

  implicit val userWrites = new Writes[User] {
    def writes(user: User) = {
      Json.obj(
        "id" -> JsNumber(user.id.get),
        "username" -> JsString(user.username),
        "email" -> JsString(user.email),
        "age" -> JsNumber(user.age))
    }
  }

  implicit val pkSerializer = new Writes[Pk[Int]] {
    def writes(id: Pk[Int]) = {
      Json.toJson(id.get)
    }
  }

  //Do not use this constructs as they are still experimental!!!
  implicit val expUserWrites = Json.writes[User]

  private def toJsonLowLevel(user: User) = {
    JsObject(
      "id" -> JsNumber(user.id.get) ::
        "username" -> JsString(user.username) ::
        "email" -> JsString(user.email) ::
        "age" -> JsNumber(user.age) :: Nil)
  }

  private def toJsonLowLevel(users: List[User]): JsObject = {
    JsObject(
      "users" -> JsArray(
        users.map { user => toJsonLowLevel(user) }) :: Nil)
  }

  private def toJsonHighLevel(users: List[User]): JsObject = {
    Json.obj(
      "users" -> Json.arr(
        users.map { user =>
          Json.obj(
            "username" -> JsString(user.username),
            "email" -> JsString(user.email),
            "age" -> JsNumber(user.age))
        }))
  }

  def edit(id: Int) = Action {
    User.load(id).map { user =>
      val bindedForm = form.fill(user)
      Ok(views.html.users.edit(bindedForm))
    }.getOrElse(NotFound)
  }

  def update(id: Int) = Action { implicit request =>
    User.load(id).map { user =>
      form.bindFromRequest.fold(
      formWithErrors => handleError(formWithErrors,views.html.users.edit(formWithErrors)),
        userWithNewValues => {
          User.update(id, userWithNewValues)
          handleSuccess("User successfully updated!")
        })
    }.getOrElse(NotFound)
  }

  def delete(id: Int) = Action { implicit request =>
    User.delete(id)
    handleSuccess("User successfully deleted!")
  }
}