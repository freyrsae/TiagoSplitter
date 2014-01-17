package controllers

import play.api.mvc.Controller
import play.api.data.Form
import play.api.data.Forms._
import models.{Demand, Demands}

/**
 * Created with IntelliJ IDEA.
 * User: freyr
 * Date: 1/13/14
 * Time: 12:33 PM
 * To change this template use File | Settings | File Templates.
 */
object MakeDemand extends Controller with Secured{

  case class DemandNoIds(amount: Int, perall: String, description: String, recipients: Seq[String])

  val demandForm = Form(
    mapping(
      "amount" -> number,
      "perall" -> text,
      "description" -> text,
      "recipients" -> seq(text)
    )(DemandNoIds.apply)(DemandNoIds.unapply)
  )

  def makeDemand = IsAuthenticated{ email => implicit request =>

    Ok(views.html.demand.makeDemand(demandForm))
  }

  def doMakeDemand = IsAuthenticated{ email => implicit request =>
    try{
      val demand = demandForm.bindFromRequest().get
      val dbDemand = Demand(userEmail = email, amount = demand.amount, perall = demand.perall, description = demand.description, recipients = demand.recipients.mkString(Demands.recipientsSeperator), status = Demands.freshDemand)
      val demandId = Demands.create(dbDemand)
      Redirect(routes.MakeDemand.show(demandId))
    }
    catch {
      case e: Exception => Redirect(routes.MakeDemand.makeDemand).flashing(
        "danger" -> "Mistókst að stofna kröfu"
      )
    }
  }

  def amountPerPersonList(amount: Int, perall: String, numberOfRecipients: Int) = {
    if(perall == "per")
      Seq.fill(numberOfRecipients)(amount)
    else
      Seq.tabulate(numberOfRecipients)(i => amount/numberOfRecipients + {if(i < amount%numberOfRecipients) 1 else 0})
  }

  // todo check that is owner of demand

  def show(demandId: Long) = IsAuthenticated{ email => implicit request =>
    val demand = Demands.findDemandById(demandId)
    val recipientsList = demand.recipients.split(Demands.recipientsSeperator)
    val amountsList = amountPerPersonList(demand.amount, demand.perall, recipientsList.size)
    Ok(views.html.demand.showDemand(demandId, demand.description, recipientsList.zip(amountsList)))
  }
  def cancel(demandId: Long) = IsAuthenticated{ email => implicit request =>
    try{
      Demands.delete(demandId)
      Redirect(routes.Application.index).flashing(
        "success" -> "Áminningu hefur verið eytt"
      )
    }
    catch {
      case e: Exception => Redirect(routes.Application.index).flashing(
        "danger" -> "Mistókst að hætta við áminningu"
      )
    }
  }


}
