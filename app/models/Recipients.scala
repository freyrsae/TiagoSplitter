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

case class Recipient(id: Option[Long] = None, demandId: Long, name: String, amount: Int, paid: Boolean)

object Recipients extends Table[Recipient]("recipients"){

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def demandId = column[Long]("demandId")
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

  private def findBy(demandId: Long) = (for { r <- Recipients if r.demandId === demandId } yield r)

  def findByDemand(demandId: Long)= DB.withSession{
    findBy(demandId).list()
  }

  def deleteByDemand(demandId: Long) = DB.withSession{
    findBy(demandId).delete
  }

  def markAsPaid(id: Long) = DB.withSession{
    findById(id).map{ r =>
      r.paid
    }.update(true)
  }
}
