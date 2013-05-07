package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation._
import anorm._
import models.User
import scala.collection.mutable.HashMap

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
      formWithErrors => BadRequest(views.html.users.add(formWithErrors)),
      user => {
        User.save(user)
        Redirect(routes.Users.list).flashing("success" -> "User successfully created!")
      })
  }

  def list = Action { implicit request =>
    Ok(views.html.users.list(User.list))
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
        formWithErrors => BadRequest(views.html.users.edit(formWithErrors)),
        userWithNewValues => {
          User.update(id, userWithNewValues)
          Redirect(routes.Users.list).flashing("success" -> "User successfully updated!")
        })
    }.getOrElse(NotFound)
  }

  def delete(id: Int) = Action {
    User.delete(id)
    Redirect(routes.Users.list).flashing("success" -> "User successfully deleted!")
  }
}