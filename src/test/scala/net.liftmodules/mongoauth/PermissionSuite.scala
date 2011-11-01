package net.liftmodules.mongoauth

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class PermissionSuite extends FunSuite with ShouldMatchers {

  test("Simple permission") {
    val perm1 = Permission("perm1")

    perm1.implies(Permission.all) should equal (true)
    perm1.implies(Permission.none) should equal (false)
    perm1.implies(Permission("perm1")) should equal (true)
    perm1.implies(Permission("a")) should equal (false)
  }

  test("Domain permission") {
    val printer = Permission("printer", "manage")

    printer.implies(Permission.all) should equal (true)
    printer.implies(Permission.none) should equal (false)
    printer.implies(Permission("printer", "query")) should equal (false)
    printer.implies(Permission("printer", "*")) should equal (true)
    printer.implies(Permission("printer", "manage")) should equal (true)
  }

  test("Entity permission") {
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