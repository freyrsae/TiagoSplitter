package utils

import com.typesafe.plugin._
import play.api.Play.current
import play.api.mvc.{RequestHeader}
import models.Demands

/**
 * Created with IntelliJ IDEA.
 * User: freyr
 * Date: 1/25/14
 * Time: 12:49 PM
 * To change this template use File | Settings | File Templates.
 */
object MailerUtil {

  def sendNotificationMail(email: String, id: String, amount: Int, description: String, recipients: String, request: RequestHeader) = {
    val mail = use[MailerPlugin].email
    mail.setSubject("Ný krafa!")
    mail.setRecipient("Tiago Splitter")
    //or use a list
    mail.setFrom("Splitter mailer")
    //sends html
    //mail.sendHtml("<html>html</html>" )
    //sends text/text
    mail.sendHtml( s"Ný rukkun var stofnuð af ${email}.<br> " +
      s"Upphæð: ${amount} <br> " +
      s"Vegna: ${description} <br> " +
      s"Móttakendur: ${recipients} <br><br><br>" +
      s" <a href=http://${request.host}/skoda/${id}>Sjá nánar</a>" )
    //sends both text and html
    //mail.send( "text", "<html>html</html>")
  }

  def sendReminderEmail(recipients: Seq[String], message: String, demandId: String, request: RequestHeader) = {
    val mail = use[MailerPlugin].email
    mail.setSubject("Áminning frá Splitter")
    mail.setRecipient(recipients.map(x => justEmail(x)):_*)
    mail.setFrom("Splitter")
    mail.sendHtml( s"${Demands.findOwnerName(demandId)} hefur sent þér áminningu úr Splitter kerfinu.<br> " +
      s"Skilaboð: ${message} <br> " +
      s" <a href=http://${request.host}/skoda/${demandId}>Sjá nánar</a>" )
  }

  def justEmail(text: String) = {
    try{
      text.split(",")(1)
    }catch {
      case e: Exception => ""
    }
  }

}
