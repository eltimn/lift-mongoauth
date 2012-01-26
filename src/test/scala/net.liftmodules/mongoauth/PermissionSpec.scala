package net.liftmodules.mongoauth

class PermissionSpec extends BaseSpec {

  "Permissions" should {

    "handle simple permission" in {
      val perm1 = Permission("perm1")

      perm1.implies(Permission.all) should equal (true)
      perm1.implies(Permission.none) should equal (false)
      perm1.implies(Permission("perm1")) should equal (true)
      perm1.implies(Permission("a")) should equal (false)
    }

    "handle domain permission" in {
      val printer = Permission("printer", "manage")

      printer.implies(Permission.all) should equal (true)
      printer.implies(Permission.none) should equal (false)
      printer.implies(Permission("printer", "query")) should equal (false)
      printer.implies(Permission("printer", "*")) should equal (true)
      printer.implies(Permission("printer", "manage")) should equal (true)
    }

    "handle entity permission" in {
      val edit = Permission("users", "edit", "abcd1234")

      edit.implies(Permission.all) should equal (true)
      edit.implies(Permission.none) should equal (false)
      edit.implies(Permission("users", "create")) should equal (false)
      edit.implies(Permission("users", "update")) should equal (false)
      edit.implies(Permission("users", "view")) should equal (false)
      edit.implies(Permission("users", Permission.wildcardToken)) should equal (true)
      edit.implies(Permission("users", "edit")) should equal (true)
      edit.implies(Permission("users", "edit", "abc")) should equal (false)
      edit.implies(Permission("users", "edit", "abcd1234")) should equal (true)
    }
  }
}
