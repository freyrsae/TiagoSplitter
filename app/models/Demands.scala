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
import utils.Validation


case class Demand(id: String,
                  userEmail: String,
                  amount: Int,
                  description: String,
                  status: String,
                  timeStamp: Timestamp = new Timestamp(System.currentTimeMillis())
                   )
{
  def getDate = {
    val format = new java.text.SimpleDateFormat("dd-MM-yyyy hh:mm")
    format.format(this.timeStamp)
  }
  def getDateNoTime = {
    val format = new java.text.SimpleDateFormat("dd-MM-yyyy")
    format.format(this.timeStamp)
  }
}

object Demands extends Table[Demand]("demands") {
  def id = column[String]("id")
  def userEmail = column[String]("user_email")
  def amount = column[Int]("amount")
  def description = column[String]("description")
  def status = column[String]("status")
  def timestamp = column[Timestamp]("timestamp")
  def * = id ~ userEmail ~ amount ~ description ~  status ~ timestamp  <> (Demand.apply _, Demand.unapply _)
  def client = foreignKey("user_demand_fk", userEmail, Users)(_.email)

  val recipientsSeperator = ";"
  val freshDemand = "Ný"

  def findById(id: String) = DB.withSession{
    for { d <- Demands if d.id === id } yield d
  }

  def findDemandById(id: String): Demand = DB.withSession{
    (for { d <- Demands if d.id === id } yield d).list().head
  }

  def create(email: String, demand: DemandNoIds): String = DB.withSession{
    val recsWithAmounts = recipientsWithAmountsList(demand)
    val demandId = Validation.getRandomAlphaNumeric
    val dbDemand = Demand(id = demandId ,userEmail = email, amount = calculateAmount(demand.amount, isPerPerson(demand.perPerson), recsWithAmounts.length), description = demand.description, status = Demands.freshDemand)
    *.insert(dbDemand)
    recsWithAmounts.map(x => Recipients.create(Recipient(demandId = demandId, name = x._1, amount = x._2, paid = false)))
    demandId

  }

  private def calculateAmount(inputAmount: Int, perPerson: Boolean, numberOfRecipients: Int): Int = {
    if(perPerson){
      inputAmount * numberOfRecipients
    }else{
      inputAmount
    }
  }

  private def isPerPerson(s: Option[String]): Boolean = {
    !s.isEmpty;
  }

  private def recipientsWithAmountsList(demand: DemandNoIds)= {
    val amountsList = amountPerPersonList(demand.amount, isPerPerson(demand.perPerson), demand.recipients.length)
    demand.recipients.zip(amountsList)
  }

  private def amountPerPersonList(amount: Int, perPerson: Boolean, numberOfRecipients: Int) = {
    if(perPerson)
      Seq.fill(numberOfRecipients)(amount)
    else
      Seq.tabulate(numberOfRecipients)(i => amount/numberOfRecipients + {if(i < amount%numberOfRecipients) 1 else 0})
  }

  def delete(id: String) = DB.withSession{
    if(findDemandById(id).status == freshDemand){
      Recipients.deleteByDemand(id)
      findById(id).delete
    }else{
      throw new UnsupportedOperationException
    }
  }

  def findByOwner(email: String) = DB.withSession{
    (for { d <- Demands if d.userEmail === email } yield d).list()
  }

  def findOwnerName(demandId: String) = DB.withSession{
    (for { d <- Demands if d.id === demandId
           u <- Users if u.email === d.userEmail} yield u).map(_.name).list().head
  }

  def findAll = DB.withSession{
    (for { d <- Demands } yield d).list()
  }

  def setNewStatus(id: String) = DB.withSession{

    def calculateStatus: String = {
      val paidRecipientsListSize = Recipients.numberOfPaidRecipients(id)
      val recipientsListSize = Recipients.numberOfRecipients(id)

      val percent = paidRecipientsListSize * 100 / recipientsListSize
      percent + "%"
    }

      findById(id).map{ d =>
        d.status
      }.update(calculateStatus)
  }

  def isOwner(id: String, user: String): Boolean = DB.withSession{
    !(for { d <- Demands if (d.id === id && d.userEmail === user) } yield d).list().isEmpty
  }
}
