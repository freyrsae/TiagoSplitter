package utils

import com.typesafe.plugin._
import play.api.Play.current

/**
 * Created with IntelliJ IDEA.
 * User: freyr
 * Date: 1/25/14
 * Time: 12:49 PM
 * To change this template use File | Settings | File Templates.
 */
object MailerUtil {

  def sendNotificationMail(email: String, amount: Int, description: String, recipients: String) = {
    val mail = use[MailerPlugin].email
    mail.setSubject("Ný krafa!")
    mail.setRecipient("memento@memento-ehf.is")
    //or use a list
    mail.setFrom("memento mailer <memento@memento-ehf.is>")
    //sends html
    //mail.sendHtml("<html>html</html>" )
    //sends text/text
    mail.send( s"Ný áminning var stofnuð af ${email}.\n Upphæð: ${amount} \n Vegna: ${description} \n Móttakendur: ${recipients}" )
    //sends both text and html
    //mail.send( "text", "<html>html</html>")
  }

}
