import models.{User, Users}
import play.api.{Application, GlobalSettings}

/**
 * Created with IntelliJ IDEA.
 * User: freyr
 * Date: 1/9/14
 * Time: 5:02 PM
 * To change this template use File | Settings | File Templates.
 */
object Global extends GlobalSettings {

  override def onStart(app: Application) {
    //    Class.forName("org.h2.Driver")
    //    ConnectionPool.singleton("jdbc:h2:mem:play", "sa", "")
    if(Users.findAll.isEmpty){
      Seq(
          User(email = "a@a.com", name = "alice", pass = "secret",kennitala = "1234567890", accountNo = "1234567890", role = Some("admin")),
          User(email = "b@b.com", name = "bob", pass = "secret",kennitala = "1234567890", accountNo = "1234567890"),
          User(email = "c@c.com", name = "chris", pass = "secret",kennitala = "1234567890", accountNo = "1234567890")
      ) foreach Users.create
    }
  }



}