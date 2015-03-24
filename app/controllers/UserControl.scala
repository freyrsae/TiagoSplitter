package controllers

import play.api.mvc.Controller
import play.api.data.Form
import play.api.data.Forms._
import views.html
import models.{User, Users}
import utils.Validation

/**
 * Created with IntelliJ IDEA.
 * User: freyr
 * Date: 1/13/14
 * Time: 12:14 PM
 * To change this template use File | Settings | File Templates.
 */
object UserControl extends Controller with Secured {

  val newUserForm = Form(
    tuple(
      "email" -> text.verifying(Validation.emailCheck),
      "name" -> text,
      "password" -> text,
      "kennitala" -> text.verifying(Validation.kennitalaCheck),
      "accountNo" -> text
    )
  )

  def admin = IsAdminAuthenticated{email => implicit request =>
    Ok(html.userControl.admin(newUserForm, Users.findAll))
  }

  def createUser = IsAdminAuthenticated{email => implicit request =>
    newUserForm.bindFromRequest().fold(
    formWithErrors => {
      BadRequest(html.userControl.admin(formWithErrors, Users.findAll))
    },
    userData => {
      try{
        Users.create(User(email = userData._1, name = userData._2, pass = userData._3, kennitala = userData._4, accountNo = userData._5))
        Redirect(routes.UserControl.admin).flashing(
          "success" -> "New user created"
        )
      }
      catch {
        case e: Exception => Redirect(routes.UserControl.admin).flashing(
          "danger" -> "Failed to create user"
        )
      }
    }
    )

  }

  val editUserForm = Form(
    tuple(
      "name" -> text,
      "password" -> text,
      "kennitala" -> text.verifying(Validation.kennitalaCheck),
      "accountNo" -> text
    )
  )

  def editUser = IsAuthenticated{email => implicit request =>
    val user = Users.findByEmail(email)
    Ok(html.userControl.editUser(editUserForm.fill(user.name, "", user.kennitala, user.accountNo)))
  }

  def doEditUser = IsAuthenticated{email => implicit request =>
    editUserForm.bindFromRequest().fold(
    formWithErrors => {
      BadRequest(html.userControl.editUser(formWithErrors))
    },
    userData => {
      try{
        Users.edit(email, userData._1, userData._2, userData._3, userData._4)
        Redirect(routes.UserControl.editUser).flashing(
          "success" -> "User info edited"
        )
      }
      catch {
        case e: Exception => Redirect(routes.UserControl.editUser).flashing(
          "danger" -> "Failed to edit user info"
        )
      }
    }
    )
  }

}
