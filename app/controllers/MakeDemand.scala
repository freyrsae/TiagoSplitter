package controllers

import play.api.mvc.Controller
import play.api.data.Form
import play.api.data.Forms._
import models.{Recipients, Demand, Demands}
import utils.MailerUtil

/**
 * Created with IntelliJ IDEA.
 * User: freyr
 * Date: 1/13/14
 * Time: 12:33 PM
 * To change this template use File | Settings | File Templates.
 */
object MakeDemand extends Controller with Secured{

  case class DemandNoIds(amount: Int, perPerson: Option[String], description: String, recipients: Seq[String])

  val demandForm = Form(
    mapping(
      "amount" -> number,
      "perPerson" -> optional(text),
      "description" -> text,
      "recipients" -> seq(text)
    )(DemandNoIds.apply)(DemandNoIds.unapply)
  )

  def makeDemand = IsAuthenticated{ email => implicit request =>

    Ok(views.html.demand.makeDemand(demandForm))
  }

  def doMakeDemand = IsAuthenticated{ email => implicit request =>
    demandForm.bindFromRequest().fold(
    formWithErrors => {
      BadRequest(views.html.demand.makeDemand(formWithErrors))
    },
    demand => {
        try{
          val demandId = Demands.create(email, demand)
          //MailerUtil.sendNotificationMail(email, demandId, demand.amount, demand.description, demand.recipients.mkString(", "), request)
          Redirect(routes.MakeDemand.show(demandId)).flashing(
            "success" -> "Stofnuð hefur verið rukkun með eftirfarandi upplýsingum"
          )
        }
        catch {
          case e: Exception => Redirect(routes.MakeDemand.makeDemand).flashing(
            "danger" -> "Mistókst að stofna rukkun"
          )
        }
      }
    )
  }

  def show(demandId: Long) = IsOwner(demandId, Demands.isOwner){ email => implicit request =>
    val demand = Demands.findDemandById(demandId)
    Ok(views.html.demand.showDemand(demand, isAdmin(request)))
  }
  def cancel(demandId: Long) = IsOwner(demandId, Demands.isOwner){ email => implicit request =>
    try{
      Demands.delete(demandId)
      Redirect(routes.Application.index).flashing(
        "success" -> "Rukkun hefur verið eytt"
      )
    }
    catch {
      case e: Exception => Redirect(routes.Application.index).flashing(
        "danger" -> "Mistókst að hætta við rukkun"
      )
    }
  }

  def listofDemands = IsAuthenticated{ email => implicit request =>

    val list = Demands.findByOwner(email).sortBy(x => x.id).reverse

    Ok(views.html.demand.listDemands(list))
  }

  def listOfDemandsAdmin = IsAdminAuthenticated{ email => implicit request =>
    val list = Demands.findAll.sortBy(x => x.id).reverse

    Ok(views.html.demand.listDemands(list, true))
  }

  def setStatusToSent(demandId: Long) = IsAdminAuthenticated{ email => implicit request =>

    Demands.setStatusToSent(demandId)
    Redirect(routes.MakeDemand.show(demandId))
  }

  def markAsPaid(recipientId: Long, demandId: Long) = IsOwner(demandId, Demands.isOwner){ email => implicit request =>

    Recipients.markAsPaid(recipientId)
    Redirect(routes.MakeDemand.show(demandId))
  }

  def markAsUnPaid(recipientId: Long, demandId: Long) = IsOwner(demandId, Demands.isOwner){ email => implicit request =>

    Recipients.markAsPaid(recipientId, false)
    Redirect(routes.MakeDemand.show(demandId))
  }


}
