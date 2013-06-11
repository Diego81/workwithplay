package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation._
import play.api.libs.json._
import anorm._
import models._
import play.api.templates.Html

object Contacts extends RenderMultipleFormats[Contact] {

  val form = Form(
    mapping(
      "id" -> ignored(NotAssigned: Pk[Int]),
      "contactType" -> nonEmptyText,
      "contact" -> nonEmptyText)(Contact.apply)(Contact.unapply))

  def add(userId: Int) = Action {
    User.load(userId).map { user =>
      Ok(views.html.contacts.add(user, form))
    }.getOrElse(NotFound)
  }

  def save(userId: Int) = Action { implicit request =>
    User.load(userId).map { user =>
      form.bindFromRequest.fold(
        formWithErrors => handleError(formWithErrors, views.html.contacts.add(user, formWithErrors)),
        contact => {
          val id = Contact.save(user, contact)
          handleSuccess(routes.Users.show(userId), "Contact successfully saved!", Map("id" -> id.toString))
        })
    }.getOrElse(NotFound)
  }

  def edit(userId: Int, id: Int) = Action {
    Contact.load(id).map { contact =>
      Ok(views.html.contacts.edit(userId, form.fill(contact)))
    }.getOrElse(NotFound)
  }

  def update(userId: Int, id: Int) = Action { implicit request =>
    Contact.load(id).map { contact =>
      form.bindFromRequest.fold(
        formWithErrors => handleError(formWithErrors, views.html.contacts.edit(userId, formWithErrors)),
        contactWithNewValues => {
          Contact.update(id, contactWithNewValues)
          handleSuccess(routes.Users.show(userId), "Contact successfully updated!")
        })
    }.getOrElse(NotFound)
  }

  def delete(userId: Int, id: Int) = Action { implicit request =>
    Contact.delete(id)
    handleSuccess(routes.Users.show(userId), "Contact successfully deleted!")
  }
}