package net.liftmodules.mongoauth
package model

class SimpleUserSpec extends WithSessionSpec with MongoTestKit {
  override val debug = false
  val userPassword = "password"

  def testUser = SimpleUser.createRecord
    .email("test@domain.com")
    .password(userPassword, true)

  "SimpleUser" should {

    "save permissions properly" in {
      val printer = Permission("printer")
      val userEntity = Permission("user.users", "read")
      val perms = List(printer, userEntity)
      val user = testUser
        .permissions(perms)
        .save()

      val userFromDb = SimpleUser.find(user.id.get)
      userFromDb.isDefined should be (true)
      userFromDb foreach { u =>
        u.permissions.get should equal (perms)
      }
    }

    "check permissions properly" in {
      val adminLogin = Permission("admin", "login")
      val adminAll = Permission("admin")
      adminLogin.implies(adminAll) should equal (true)
      adminLogin.implies(Set(adminAll)) should equal (true)

      val user = SimpleUser.createRecord.permissions(List(Permission("admin")))
      SimpleUser.logUserIn(user, false, false)
      SimpleUser.hasPermission(adminLogin) should equal (true)
    }
  }
}
