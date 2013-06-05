package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation._
import play.api.libs.json._
import anorm._
import models._
import play.api.templates.Html

object Users extends RenderMultipleFormats[User] {

  val form = Form(
    mapping(
      "id" -> ignored(NotAssigned: Pk[Int]),
      "username" -> nonEmptyText,
      "age" -> number)(User.apply)(User.unapply))

  def list = Action { implicit request =>
    render {
      case Accepts.Html() => Ok(views.html.users.list(User.list))
      case Accepts.Json() => Ok(Json.toJson(User.list))
    }
  }

  implicit val userWrites = new Writes[User] {
    def writes(user: User) = {
      Json.obj(
        "id" -> JsNumber(user.id.get),
        "username" -> JsString(user.username),
        "age" -> JsNumber(user.age))
    }
  }
  
  val extendedUserWrites = new Writes[User] {
    def writes(user: User) = {
      userWrites.writes(user) + ("contacts" -> Writes.seq(contactWrites).writes(user.contacts))
    }
  }

  val contactWrites = new Writes[Contact] {
    def writes(contact: Contact) = {
      Json.obj(
        "id" -> JsNumber(contact.id.get),
        "contactType" -> JsString(contact.contactType),
        "contact" -> JsString(contact.contact))
    }
  }
  
  def show(id: Int) = Action { implicit request =>
    User.load(id).map { user =>
    render {
      case Accepts.Html() => Ok(views.html.users.show(user))
      case Accepts.Json() => Ok(Json.toJson(user)(extendedUserWrites))
    }
    }.getOrElse(NotFound)    
  }
  
  def add = Action {
    Ok(views.html.users.add(form))
  }

  def save = Action { implicit request =>
    form.bindFromRequest.fold(
      formWithErrors => handleError(formWithErrors, views.html.users.add(formWithErrors)),
      user => {
        User.save(user)
        handleSuccess(routes.Users.list, "User successfully created!")
      })
  }
  
  def edit(id: Int) = Action {
    User.load(id).map { user =>
      val bindedForm = form.fill(user)
      Ok(views.html.users.edit(id, bindedForm))
    }.getOrElse(NotFound)
  }

  def update(id: Int) = Action { implicit request =>
    User.load(id).map { user =>
      form.bindFromRequest.fold(
      formWithErrors => handleError(formWithErrors,views.html.users.edit(id, formWithErrors)),
        userWithNewValues => {
          User.update(id, userWithNewValues)
          handleSuccess(routes.Users.show(id), "User successfully updated!")
        })
    }.getOrElse(NotFound)
  }

  def delete(id: Int) = Action { implicit request =>
    User.delete(id)
    handleSuccess(routes.Users.list, "User successfully deleted!")
  }
}
