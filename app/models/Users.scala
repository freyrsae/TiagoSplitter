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
import org.mindrot.jbcrypt.BCrypt


case class User(email: String, name: String, pass: String, kennitala: String, accountNo: String, role: Option[String] = None){
  def hashPass: User = {
    User(this.email, this.name, BCrypt.hashpw(this.pass, BCrypt.gensalt()), this.kennitala, this.accountNo, this.role)
  }
}

object Users extends Table[User]("users") {
  def email = column[String]("email", O.PrimaryKey)
  def name = column[String]("name")
  def pass = column[String]("pass")
  def kennitala = column[String]("kennitala")
  def accountNo = column[String]("account_no")
  def role = column[Option[String]]("role")

  def * = email ~ name ~ pass ~ kennitala ~ accountNo ~ role <> (User.apply _, User.unapply _)

  def findByEmail(email: String): User = DB.withSession{
    val list = ( for{ r <- Users if r.email === email} yield r ).list()
    list match {
      case Nil => null
      case _ => list.head
    }
  }

  def findAll: List[User] = DB.withSession{
    (for{u <- Users} yield  u).list()
  }

  def create(user: User) = DB.withSession{
    Users.insert(user.hashPass)
  }

  def edit(email: String, name: String, password: String, kennitala: String, accountNo: String) = DB.withSession{
      val all = findAll
      if(!password.isEmpty){
        for(a <- all){
          Query(Users).filter(_.email === email).map{ r =>
            r.name ~ r.pass ~ r.kennitala ~ r.accountNo
          }.update(name, BCrypt.hashpw(password, BCrypt.gensalt()), kennitala, accountNo)
        }
      }else{
        for(a <- all){
          Query(Users).filter(_.email === email).map{ r =>
            r.name ~ r.kennitala ~ r.accountNo
          }.update(name, kennitala, accountNo)
        }
      }
  }

  def authenticate(email: String, password: String): Boolean = DB.withSession{
    val user = Users.findByEmail(email)
    user != null && BCrypt.checkpw(password, user.pass)
  }

  def isAdmin(email: String): Boolean = DB.withSession{
    val user = Users.findByEmail(email)
    user != null && user.role == Some("admin")
  }
}
