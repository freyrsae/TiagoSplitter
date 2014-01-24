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
import controllers.MakeDemand.DemandNoIds
import java.sql.Timestamp


case class Demand(id: Option[Long] = None,
                  userEmail: String,
                  amount: Int,
                  perall: String,
                  description: String,
                  status: String,
                  timeStamp: Timestamp = new Timestamp(System.currentTimeMillis())
                   )
{
  def getDate = {
    val format = new java.text.SimpleDateFormat("dd-MM-yyyy")
    format.format(this.timeStamp)
  }
}

object Demands extends Table[Demand]("demands") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def userEmail = column[String]("user_email")
  def amount = column[Int]("amount")
  def perall = column[String]("perall")
  def description = column[String]("description")
  def status = column[String]("status")
  def timestamp = column[Timestamp]("timestamp")
  def * = id.? ~ userEmail ~ amount ~ perall ~ description ~  status ~ timestamp  <> (Demand.apply _, Demand.unapply _)
  def autoInc = id.? ~ userEmail ~ amount ~ perall ~ description ~ status ~ timestamp <> (Demand.apply _, Demand.unapply _) returning id
  def client = foreignKey("user_demand_fk", userEmail, Users)(_.email)

  val recipientsSeperator = ";"
  val freshDemand = "Ný"
  val sentDemand = "Send"

  def findById(id: Long) = DB.withSession{
    for { d <- Demands if d.id === id } yield d
  }

  def findDemandById(id: Long): Demand = DB.withSession{
    (for { d <- Demands if d.id === id } yield d).list().head
  }

  def create(email: String, demand: DemandNoIds): Long = DB.withSession{
    val dbDemand = Demand(userEmail = email, amount = demand.amount, perall = demand.perall, description = demand.description, status = Demands.freshDemand)
    val demandId = autoInc.insert(dbDemand)
    val recsWithAmounts = recipientsWithAmountsList(demand)
    recsWithAmounts.map(x => Recipients.create(Recipient(demandId = demandId, name = x._1, amount = x._2, paid = false)))
    demandId
  }

  private def recipientsWithAmountsList(demand: DemandNoIds)= {
    val amountsList = amountPerPersonList(demand.amount, demand.perall, demand.recipients.length)
    demand.recipients.zip(amountsList)
  }

  private def amountPerPersonList(amount: Int, perall: String, numberOfRecipients: Int) = {
    if(perall == "per")
      Seq.fill(numberOfRecipients)(amount)
    else
      Seq.tabulate(numberOfRecipients)(i => amount/numberOfRecipients + {if(i < amount%numberOfRecipients) 1 else 0})
  }

  def delete(id: Long) = DB.withSession{
    if(findDemandById(id).status == freshDemand){
      findById(id).delete
    }else{
      throw new UnsupportedOperationException
    }
  }

  def findByOwner(email: String) = DB.withSession{
    (for { d <- Demands if d.userEmail === email } yield d).list()
  }

  def findAll = DB.withSession{
    (for { d <- Demands } yield d).list()
  }

  def setStatusToSent(id: Long) = DB.withSession{
    findById(id).map{ d =>
      d.status
    }.update(sentDemand)
  }

  def isOwner(id: Long, user: String): Boolean = DB.withSession{
    print(id)
    print(user)
    !(for { d <- Demands if (d.id === id && d.userEmail === user) } yield d).list().isEmpty
  }
}
