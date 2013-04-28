package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation._
import models.User
import scala.collection.mutable.HashMap

object Users extends Controller {

  val users: scala.collection.mutable.Map[Int, User] = new HashMap

  val form = Form(
    mapping(
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
        users.put(user.id, user)
        Redirect(routes.Users.list).flashing("success" -> "User successfully created!")
      })
  }

  def list = Action { implicit request =>
    Ok(views.html.users.list(users.values))
  }

  def edit(id: Int) = Action {
    users.get(id).map { user =>
      val bindedForm = form.fill(user)
      Ok(views.html.users.edit(bindedForm))
    }.getOrElse(NotFound)
  }

  def update(id: Int) = Action { implicit request =>
    users.get(id).map { user =>
      form.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.users.edit(formWithErrors)),
        user => {
          user.id = id
          users.put(id, user)
          Redirect(routes.Users.list).flashing("success" -> "User successfully updated!")
        })
    }.getOrElse(NotFound)
  }

  def delete(id: Int) = Action {
    users.remove(id)
    Redirect(routes.Users.list).flashing("success" -> "User successfully deleted!")
  }

  // The following actions have nothing to do with the application logic.
  // They are there just to show some alternative approaches to access a request parameter.
  def echo = Action { request =>
    Ok(request.queryString.get("message").get.head)
  }

  def echoPost = Action { request =>
    val formMap = request.body.asFormUrlEncoded.get
    Ok(formMap.get("message").get.head)
  }

}