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
      "contactType" -> mappings.CustomMappings.contactType,
      "contact" -> nonEmptyText)(Contact.apply)(Contact.unapply))

  def add(userId: Int) = Action {
    User.load(userId).map { user =>
      Ok(views.html.contacts.add(user, form))
    }.getOrElse(NotFound)
  }

  def save(userId: Int) = Action { implicit request =>
    User.load(userId).map { user =>
      val bindedForm = form.bindFromRequest
      bindedForm.fold(
        formWithErrors => handleError(formWithErrors, views.html.contacts.add(user, formWithErrors)),
        contact => {
          validateContact(bindedForm, contact).fold(
            formWithErrors => handleError(formWithErrors, views.html.contacts.add(user, formWithErrors)),
            msg => {
              val id = Contact.save(user, contact)
              handleSuccess(routes.Users.show(userId), "Contact successfully saved!", Map("id" -> id.toString))
            })
        })
    }.getOrElse(NotFound)
  }

  private def validateContact(form: Form[Contact], contact: Contact): Either[Form[Contact], String] = {
    val either = contact.contactType match {
      case ContactType.Email => validateEmail(contact.contact)
      case ContactType.Telephone => validateTelephone(contact.contact)
      case ContactType.Address => validateAddress(contact.contact)
    }
    either.fold(
      error => Left(form.withError(error)),
      msg => Right(msg))
  }

  private val contactKey = "contact"

  private def validateEmail(email: String): Either[FormError, String] = {
    """(\w+)@([\w\.]+)""".r.findFirstIn(email) match {
      case None => Left(new FormError(contactKey, email + " is not a valid email address"))
      case Some(value) => Right("Correct email address")
    }
  }

  private def validateTelephone(telephone: String): Either[FormError, String] = {
    """[0-9]{10}""".r.findFirstIn(telephone) match {
      case None => Left(new FormError(contactKey, telephone + " is not a valid telephone number"))
      case Some(value) => Right("Correct telephone address")
    }
  }

  private def validateAddress(address: String): Either[FormError, String] = {
    if (address.length < 10)
      Left(new FormError(contactKey, address + " is not a valid address"))
    else
      Right("Valid address")
  }

  def edit(userId: Int, id: Int) = Action {
    Contact.load(id).map { contact =>
      Ok(views.html.contacts.edit(userId, id, form.fill(contact)))
    }.getOrElse(NotFound)
  }

  def update(userId: Int, id: Int) = Action { implicit request =>
    Contact.load(id).map { contact =>
      val bindedForm = form.bindFromRequest
      bindedForm.fold(
        formWithErrors => handleError(formWithErrors, views.html.contacts.edit(userId, id, formWithErrors)),
        contactWithNewValues => {
          validateContact(bindedForm, contactWithNewValues).fold(
            formWithErrors => handleError(formWithErrors, views.html.contacts.edit(userId, id, formWithErrors)),
            msg => {
              Contact.update(id, contactWithNewValues)
              handleSuccess(routes.Users.show(userId), "Contact successfully updated!")
            })
        })
    }.getOrElse(NotFound)
  }

  def delete(userId: Int, id: Int) = Action { implicit request =>
    Contact.delete(id)
    handleSuccess(routes.Users.show(userId), "Contact successfully deleted!")
  }
}