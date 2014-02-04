package controllers

import play.api.mvc.Controller
import play.api.data.Form
import play.api.data.Forms._
import models.{Contact, Contacts, Demand, Demands}
import scala.util.parsing.json.JSONArray


/**
 * Created with IntelliJ IDEA.
 * User: freyr
 * Date: 1/13/14
 * Time: 12:33 PM
 * To change this template use File | Settings | File Templates.
 */
object ContactControl extends Controller with Secured{

  val contactForm = Form(
    tuple(
      "kennitala" -> text,
      "name" -> text,
      "email" -> text
    )
  )

  def createContact = IsAuthenticated{ email => implicit request =>
    Ok(views.html.contacts.createContact(contactForm, Contacts.findByUser(email).sortBy(x => x.name)))
  }

  def doCreateContact = IsAuthenticated{ email => implicit request =>
    try{
      val contact = contactForm.bindFromRequest().get
      Contacts.create(Contact(kennitala = contact._1, name = contact._2, contactEmail = contact._3, userEmail = email))
      Redirect(routes.ContactControl.createContact).flashing(
        "success" -> "Tengilið hefur verið bætt við"
      )
    }
    catch {
      case e: Exception => Redirect(routes.ContactControl.createContact).flashing(
        "danger" -> "Mistókst að bæta við tengilið"
      )
    }
  }

  def searchContacts(term: String) = IsAuthenticated{ email => implicit request =>
    val jsonArr = new JSONArray(Contacts.searchInContacts(term, email).map(x => s"${x.name}, ${x.contactEmail}, ${x.kennitala}"))
    Ok(jsonArr.toString())
  }

}
