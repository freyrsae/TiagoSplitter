package models


/**
 * Created with IntelliJ IDEA.
 * User: freyr
 * Date: 1/17/14
 * Time: 3:22 PM
 * To change this template use File | Settings | File Templates.
 */

import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.Play.current
import Database.threadLocalSession

case class Contact(id: Option[Long] = None, kennitala: String, name: String, phone: String, userEmail: String)

object Contacts extends Table[Contact]("contacts"){

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def kennitala = column[String]("kennitala")
  def name = column[String]("name")
  def phone = column[String]("phone")
  def userEmail = column[String]("userEmail")

  def * = id.? ~ kennitala ~ name ~ phone ~ userEmail <> (Contact.apply _, Contact.unapply _)
  def client = foreignKey("user_contact_fk", userEmail, Users)(_.email)

  def create(contact: Contact) = DB.withSession{
    *.insert(contact)
  }

  def delete(id: Long) = DB.withSession{
    findById(id).delete
  }

  def edit(id: Long, contact: Contact) = DB.withSession{
    findById(id).update(contact)
  }

  def findById(id: Long) = {
    (for {c <- Contacts if c.id === id} yield c)
  }

  def findContactById(id: Long) = DB.withSession{
    findById(id).list().head
  }

  def findByUser(email: String) = DB.withSession{
    (for {c <- Contacts if c.userEmail === email} yield c).list()
  }

  def searchInContacts(term: String, email: String) = DB.withSession{
    (for {c <- Contacts if (c.userEmail === email && (c.name like "%" + term + "%"))} yield c).list()
  }

  def isOwner(id: String, user: String): Boolean = DB.withSession{
    !(for { c <- Contacts if (c.id === id.toLong && c.userEmail === user) } yield c).list().isEmpty
  }
}
