package controllers

import play.api.mvc.{Action, Controller}
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
          MailerUtil.sendNotificationMail(email, demandId, demand.amount, demand.description, demand.recipients.mkString(", "), request)
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

  def show(demandId: String) = Action{ implicit request =>
    val demand = Demands.findDemandById(demandId)
    Ok(views.html.demand.showDemand(demand, isAdmin(request), Demands.isOwner(demandId, request.session.get("email").getOrElse(""))))
  }

  def cancel(demandId: String) = IsOwner(demandId, Demands.isOwner){email => implicit request =>
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

    val list = Demands.findByOwner(email).sortBy(x => x.timeStamp.getTime).reverse

    Ok(views.html.demand.listDemands(list))
  }

  def listOfDemandsAdmin = IsAdminAuthenticated{ email => implicit request =>
    val list = Demands.findAll.sortBy(x => x.id).reverse

    Ok(views.html.demand.listDemands(list, true))
  }

  def setStatusToSent(demandId: String) = IsAdminAuthenticated{ email => implicit request =>

    Demands.setNewStatus(demandId)
    Redirect(routes.MakeDemand.show(demandId))
  }

  def markAsPaid(recipientId: Long, demandId: String) = IsOwner(demandId, Demands.isOwner){ email => implicit request =>

    Recipients.markAsPaid(recipientId)
    Redirect(routes.MakeDemand.show(demandId))
  }

  def markAsUnPaid(recipientId: Long, demandId: String) = IsOwner(demandId, Demands.isOwner){ email => implicit request =>

    Recipients.markAsPaid(recipientId, false)
    Redirect(routes.MakeDemand.show(demandId))
  }

  val reminderForm = Form(
    tuple(
      "recipients" -> seq(text),
      "message" -> text
    )
  )

  def sendReminder(demandId: String) = IsOwner(demandId, Demands.isOwner){ email => implicit request =>

    reminderForm.bindFromRequest.fold(
      formWithErrors => Redirect(routes.MakeDemand.show(demandId)).flashing(
        "danger" -> "Ekki tókst að senda áminningu"
      ),
      reminder => try{
        MailerUtil.sendReminderEmail(reminder._1, reminder._2, demandId, request)
        Redirect(routes.MakeDemand.show(demandId)).flashing("success" -> "Áminning hefur verið send")
      } catch {
        case e: Exception => Redirect(routes.MakeDemand.show(demandId)).flashing("danger" -> "Ekki tókst að senda áminningu")
      }

    )
  }
}
