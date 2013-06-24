package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Redirect(routes.Users.list)
  }

  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(
      Routes.javascriptRouter("jsRoutes")(
        Users.update,
        Contacts.save,
        Contacts.update,
        Contacts.delete)).as("text/javascript")
  }

}