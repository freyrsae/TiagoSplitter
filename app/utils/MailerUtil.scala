package utils

import com.typesafe.plugin._
import play.api.Play.current
import play.api.mvc.{RequestHeader}

/**
 * Created with IntelliJ IDEA.
 * User: freyr
 * Date: 1/25/14
 * Time: 12:49 PM
 * To change this template use File | Settings | File Templates.
 */
object MailerUtil {

  def sendNotificationMail(email: String, id: Long, amount: Int, description: String, recipients: String, request: RequestHeader) = {
    val mail = use[MailerPlugin].email
    mail.setSubject("Ný krafa!")
    mail.setRecipient("memento@memento-ehf.is")
    //or use a list
    mail.setFrom("memento mailer <memento@memento-ehf.is>")
    //sends html
    //mail.sendHtml("<html>html</html>" )
    //sends text/text
    mail.sendHtml( s"Ný áminning var stofnuð af ${email}.<br> " +
      s"Upphæð: ${amount} <br> " +
      s"Vegna: ${description} <br> " +
      s"Móttakendur: ${recipients} <br>" +
      s" <a href=http://${request.host}/skoda/${id}>Sjá nánar</a>" )
    //sends both text and html
    //mail.send( "text", "<html>html</html>")
  }

}
