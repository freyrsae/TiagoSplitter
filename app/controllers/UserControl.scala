package controllers

import play.api.mvc.Controller
import play.api.data.Form
import play.api.data.Forms._
import views.html
import models.{User, Users}

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
      "email" -> text,
      "name" -> text,
      "password" -> text
    )
  )

  def admin = IsAdminAuthenticated{email => implicit request =>
    Ok(html.userControl.admin(newUserForm, Users.findAll))
  }

  def createUser = IsAdminAuthenticated{email => implicit request =>
    try{
      val newUser = newUserForm.bindFromRequest().get
      Users.create(User(email = newUser._1, name = newUser._2, pass = newUser._3))
      Redirect(routes.UserControl.admin).flashing(
        "success" -> "Nýr notandi hefur verið gerður"
      )
    }
    catch {
      case e: Exception => Redirect(routes.UserControl.admin).flashing(
        "danger" -> "Mistókst að gera nýjan notanda"
      )
    }

  }

  val editUserForm = Form(
    tuple(
      "name" -> text,
      "password" -> text
    )
  )

  def editUser = IsAuthenticated{email => implicit request =>
    Ok(html.userControl.editUser(editUserForm))
  }

  def doEditUser = IsAuthenticated{email => implicit request =>
    try{
      val editUser = editUserForm.bindFromRequest().get
      Users.edit(email, editUser._1, editUser._2)
      Redirect(routes.UserControl.editUser).flashing(
        "success" -> "Notendaupplýsingum hefur verið breytt"
      )
    }
    catch {
      case e: Exception => Redirect(routes.UserControl.editUser).flashing(
        "danger" -> "Mistókst að breyta notendaupplýsingum"
      )
    }
  }

}
