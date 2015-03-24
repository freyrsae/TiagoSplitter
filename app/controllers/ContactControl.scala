package controllers

import play.api.mvc.Controller
import play.api.data.Form
import play.api.data.Forms._
import models._
import scala.util.parsing.json.JSONArray
import utils.Validation
import scala.util.parsing.json.JSONArray
import scala.Some
import models.Contact


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
      "kennitala" -> text.verifying(Validation.kennitalaCheck),
      "name" -> text,
      "phone" -> text.verifying(Validation.emailCheck)
    )
  )

  def createContact = IsAuthenticated{ email => implicit request =>
    Ok(views.html.contacts.createContact(contactForm, Contacts.findByUser(email).sortBy(x => x.name)))
  }

  def doCreateContact = IsAuthenticated{ email => implicit request =>
    contactForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.contacts.createContact(formWithErrors, Contacts.findByUser(email).sortBy(x => x.name)))
    },
      contactData => {
        try{
          Contacts.create(Contact(kennitala = contactData._1, name = contactData._2, phone = contactData._3, userEmail = email))
          Redirect(routes.ContactControl.createContact).flashing(
            "success" -> "Contact added"
          )
        }
        catch {
          case e: Exception => Redirect(routes.ContactControl.createContact).flashing(
            "danger" -> "Failed to add contact"
          )
        }
    }
    )
  }

  def editContact(id: Long) = IsOwner(id.toString, Contacts.isOwner){ email => implicit request =>
    val contact = Contacts.findContactById(id)
    Ok(views.html.contacts.editContact(id, contactForm.fill(contact.kennitala, contact.name, contact.phone)))

  }

  def doEditContact(id: Long) = IsOwner(id.toString, Contacts.isOwner){ email => implicit request =>

    try{
      val editContact = contactForm.bindFromRequest().get
      Contacts.edit(id, Contact(id = Some(id), kennitala = editContact._1, name = editContact._2, phone = editContact._3, userEmail = email))
      Redirect(routes.ContactControl.createContact).flashing(
        "success" -> "Contact info edited"
      )
    }
    catch {
      case e: Exception => Redirect(routes.ContactControl.createContact).flashing(
        "danger" -> "Failed to edit contact info"
      )
    }


  }

  def deleteContact(id: Long) = IsOwner(id.toString, Contacts.isOwner){ email => implicit request =>
    try{
      Contacts.delete(id)
      Redirect(routes.ContactControl.createContact).flashing(
      "success" -> "Contact deleted"
      )
    }
    catch {
      case e: Exception => Redirect(routes.ContactControl.createContact).flashing(
        "danger" -> "Failed to delete contact"
      )
    }
  }

  def searchContacts(term: String) = IsAuthenticated{ email => implicit request =>
    val jsonArr = new JSONArray(Contacts.searchInContacts(term, email).map(x => s"${x.name}, ${x.phone}, ${x.kennitala}"))
    Ok(jsonArr.toString())
  }

  def searchReminderContacts(demandId: String, term: String) = IsAuthenticated{ email => implicit request =>
    val jsonArr = new JSONArray(Recipients.reminderSearch(demandId, term).map(_.name))
    Ok(jsonArr.toString())
  }

}
