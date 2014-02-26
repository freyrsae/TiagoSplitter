package models

/**
 * Created with IntelliJ IDEA.
 * User: freyr
 * Date: 1/24/14
 * Time: 10:28 AM
 * To change this template use File | Settings | File Templates.
 */

import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.Play.current
import Database.threadLocalSession

case class Recipient(id: Option[Long] = None, demandId: String, name: String, amount: Int, paid: Boolean){
  def justName = {
    this.name.split(",")(0)
  }
}

object Recipients extends Table[Recipient]("recipients"){

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def demandId = column[String]("demandId")
  def name = column[String]("name")
  def amount = column[Int]("amount")
  def paid = column[Boolean]("paid")
  def * = id.? ~ demandId ~ name ~ amount ~ paid <> (Recipient.apply _, Recipient.unapply _)
  def demand = foreignKey("demand_recipient_fk", demandId, Demands)(_.id)

  def findById(id: Long) = DB.withSession{
    for { r <- Recipients if r.id === id } yield r
  }

  def create(recipient: Recipient)=DB.withSession{
    *.insert(recipient)
  }

  private def findBy(demandId: String) = (for { r <- Recipients if r.demandId === demandId } yield r)

  def findByDemand(demandId: String)= DB.withSession{
    findBy(demandId).list()
  }

  def reminderSearch(demandId: String, term: String) = DB.withSession{
    findBy(demandId).filter(_.name like "%" + term + "%").list()
  }

  def numberOfPaidRecipients(demandId: String) = DB.withSession{
    Recipients.findBy(demandId).filter(_.paid === true).list().length
  }

  def numberOfRecipients(demandId: String) = DB.withSession{
    Recipients.findBy(demandId).list().length
  }

  def deleteByDemand(demandId: String) = DB.withSession{
    findBy(demandId).delete
  }

  def markAsPaid(id: Long, paid: Boolean = true) = DB.withSession{
    findById(id).map{ r =>
      r.paid
    }.update(paid)

    val demandId = findById(id).map(_.demandId).first()
    if(Demands.findById(demandId).map(_.status).first() != Demands.freshDemand){
      Demands.setNewStatus(findById(id).map(_.demandId).first())
    }
  }
}
