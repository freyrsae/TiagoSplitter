package controllers

import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import views.html
import models.{Demands, User, Users}
import play.mvc.Results.Redirect
import utils.Validation

object Application extends Controller with Secured {

  val loginForm = Form(
    tuple(
      "email" -> text.verifying(Validation.emailCheck),
      "password" -> text
    ) verifying ("Invalid email or password", result => result match {
      case (email, password) => Users.authenticate(email, password)
    })
  )


  /**
   * Login page.
   */
  def login = Action { implicit request =>
    Ok(html.login(loginForm))
  }

  /**
   * Handle login form submission.
   */
  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      user => Redirect(routes.Application.index).withSession("email" -> user._1)
    )
  }

  def index = IsAuthenticated{ email => implicit request =>
    Ok(views.html.mainPage.overview(isAdmin(request)))
  }

  def logout = Action{
    Redirect(routes.Application.login).withNewSession.flashing(
      "success" -> "User logged out"
    )
  }

  def aboutUs = Action{ implicit  request =>

    Ok(views.html.aboutUs(isLoggedIn(request)))
  }

}

trait Secured {


  def isLoggedIn(request: RequestHeader): Boolean = Users.findByEmail(clientId(request).getOrElse("")) != null

  def isAdmin(request: RequestHeader): Boolean = Users.isAdmin(clientId(request).getOrElse(""))

  /**
   * Retrieve the connected user email.
   */
  private def clientId(request: RequestHeader) = request.session.get("email")

  /**
   * Redirect to login if the user in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.login)

  // --

  /**
   * Action for authenticated users.
   */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(clientId, onUnauthorized) { client =>
    Action(request => f(client)(request))
  }

  def IsAdminAuthenticated(f: => String => Request[AnyContent] => Result) = IsAuthenticated { client => request =>
    if(Users.isAdmin(client)) {
      f(client)(request)
    } else {
      Results.Forbidden("You do not have permission to do this")
    }
  }

  def IsOwner(id: String, isOwnerCheck: (String, String) => Boolean, errorMessage: String = "You are not the owner")(f: => String => Request[AnyContent] => Result) = IsAuthenticated { client => request =>
    if(isOwnerCheck(id, client) || Users.isAdmin(client)) {
      f(client)(request)
    } else {
      Results.Forbidden(errorMessage)
    }
  }


}