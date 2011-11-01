package net.liftmodules.mongoauth

import model.SimpleUser

import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.matchers.ShouldMatchers

class SimpleUserSuite extends FunSuite with MongoTestKit with ShouldMatchers {
  //override val debug = true
  val userPassword = "password"

  def testUser = SimpleUser.createRecord
    .email("test@domain.com")
    .password(userPassword, true)

  test("SimpleUser saves permissions properly") {
    val printer = Permission("printer")
    val userEntity = Permission("user.users", "read")
    val perms = List(printer, userEntity)
    val user = testUser
      .permissions(perms)
      .save
    val userFromDb = SimpleUser.find(user.id.is)
    userFromDb should be ('defined)
    userFromDb foreach { u =>
      u.permissions.is should equal (perms)
    }
  }
}