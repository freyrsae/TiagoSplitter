package models

/**
 * Created with IntelliJ IDEA.
 * User: freyr
 * Date: 11/11/13
 * Time: 12:58 PM
 * To change this template use File | Settings | File Templates.
 */
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.Play.current
import Database.threadLocalSession


case class Demand(id: Option[Long] = None, userEmail: String, amount: Int, perall: String, description: String, recipients: String, status: String)

object Demands extends Table[Demand]("demands") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def userEmail = column[String]("user_email")
  def amount = column[Int]("amount")
  def perall = column[String]("perall")
  def description = column[String]("description")
  def recipients = column[String]("recipients")
  def status = column[String]("status")
  def * = id.? ~ userEmail ~ amount ~ perall ~ description ~ recipients ~ status  <> (Demand.apply _, Demand.unapply _)
  def autoInc = id.? ~ userEmail ~ amount ~ perall ~ description ~ recipients ~ status <> (Demand.apply _, Demand.unapply _) returning id
  def client = foreignKey("user_demand_fk", userEmail, Users)(_.email)

  val recipientsSeperator = ";"
  val freshDemand = "fresh"

  def findById(id: Long) = DB.withSession{
    for { d <- Demands if d.id === id } yield d
  }

  def findDemandById(id: Long): Demand = DB.withSession{
    (for { d <- Demands if d.id === id } yield d).list().head
  }

  def create(demand: Demand): Long = DB.withSession{
   autoInc.insert(demand)
  }

  def delete(id: Long) = DB.withSession{
    if(findDemandById(id).status == freshDemand){
      findById(id).delete
    }else{
      throw new UnsupportedOperationException
    }
  }
}
