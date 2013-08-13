package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation._
import models._

class UserGroups(userGroupManager: UserGroupManager) extends Controller {

  val form = Form(
    mapping(
      "id" -> ignored(anorm.NotAssigned: anorm.Pk[Int]),
      "name" -> nonEmptyText,
      "description" -> nonEmptyText)(UserGroup.apply)(UserGroup.unapply))

  def add = Action {
    Ok(views.html.usergroups.add(form))
  }

  def save = Action { implicit request =>
    form.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.usergroups.add(formWithErrors)),
      usergroups => {
        userGroupManager.save(usergroups)
        Redirect(routes.UserGroups.list).flashing("success" -> "Group successfully created!")
      })
  }

  def list = Action { implicit request =>
    Ok(views.html.usergroups.list(userGroupManager.list))
  }

  def show(id: Int) = Action { implicit request =>
    userGroupManager.load(id).map { group =>
      Ok(views.html.usergroups.show(group))
    }.getOrElse(NotFound)
  }

  def edit(id: Int) = Action {
    userGroupManager.load(id).map { group =>
      Ok(views.html.usergroups.edit(id, form.fill(group)))
    }.getOrElse(NotFound)
  }

  def update(id: Int) = Action { implicit request =>
    userGroupManager.load(id).map { group =>
    form.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.usergroups.edit(id, formWithErrors)),
      groupWithNewValues => {
        userGroupManager.update(id, groupWithNewValues)
        Redirect(routes.UserGroups.show(id)).flashing("success" -> "Group successfully update!")
      })
    }.getOrElse(NotFound)
  }

  def delete(id: Int) = Action {
    userGroupManager.delete(id)
    Redirect(routes.UserGroups.list).flashing("success" -> "Group successfully deleted!")
  }
}