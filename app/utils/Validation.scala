package utils

import play.api.data.validation._
import java.security.SecureRandom
import java.math.BigInteger
;


/**
 * Created with IntelliJ IDEA.
 * User: freyr
 * Date: 2/5/14
 * Time: 1:57 PM
 * To change this template use File | Settings | File Templates.
 */
object Validation {
  val allNumbers = """\d*""".r
  val allLetters = """[A-Za-z]*""".r
  val someNonKennitalaDigits = """\d{6}[ -]*\d{4}""".r
  val containsNoAt = """[^@]*""".r
  final val randomGenerator = new SecureRandom()

  val passwordCheckConstraint: Constraint[String] = Constraint("constraints.passwordcheck")({
    plainText =>
      val errors = plainText match {
        case allNumbers() => Seq(ValidationError("Password is all numbers"))
        case allLetters() => Seq(ValidationError("Password is all letters"))
        case _ => Nil
      }
      if (errors.isEmpty) {
        Valid
      } else {
        Invalid(errors)
      }
  })

  val kennitalaCheck: Constraint[String] = Constraint("contraints.kennitala")({
    text =>
      val errors = text match {
        case someNonKennitalaDigits() => Nil
        case _ => Seq(ValidationError("Óleyfileg kennitala"))
      }
      if (errors.isEmpty) {
        Valid
      } else {
        Invalid(errors)
      }
  })

  val emailCheck: Constraint[String] = Constraint("constraints.email")({
    text =>
      val errors = text match {
        case containsNoAt() => Seq(ValidationError("Netfang verður að innihalda @"))
        case _ => Nil
      }
      if(errors.isEmpty){
        Valid
      } else {
        Invalid(errors)
      }
  })

  val getRandomAlphaNumeric = {
    new BigInteger(130, randomGenerator).toString(32)
  }

}
