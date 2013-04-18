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
      "username" -> text,
      "email" -> text)(User.apply)(User.unapply))

  def add = Action {
    Ok(views.html.users.add(form))
  }
  
  def save = Action{ implicit request =>
    val user = form.bindFromRequest.get
    users.put(user.id, user)
    Redirect(routes.Users.list)
  }

  def list = Action {
    Ok(views.html.users.list(users.values))
  }
  
  def edit(id: Int) = Action {
    val bindedForm = form.fill(users.get(id).get)
    Ok(views.html.users.edit(bindedForm))
  }
  
  def update(id: Int) = Action { implicit request =>
    val user = form.bindFromRequest.get
    user.id = id
    users.put(id, user)
    Redirect(routes.Users.list)
  }
  
  def delete(id: Int) = Action {
    users.remove(id)
    Redirect(routes.Users.list)
  }
}