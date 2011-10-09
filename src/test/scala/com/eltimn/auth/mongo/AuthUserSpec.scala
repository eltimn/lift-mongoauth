package com.eltimn
package auth.mongo

import org.apache.shiro.subject.Subject
import org.apache.shiro.authc.{IncorrectCredentialsException, UnknownAccountException, UsernamePasswordToken}
import org.specs.Specification

object AuthUserSpec extends Specification with ShiroTestKit with MongoTestKit {
  //override val debug = true
  
  val userPassword = "password"

  def testUser = SimpleUser.createRecord
		.email("test@domain.com")
		.password(userPassword, true)

	"MongoRealm" should {

		doBefore {
			// 1.  Build the Subject instance for the test to run:
			val subjectUnderTest = new Subject.Builder(securityManager).buildSubject
			// 2. Bind the subject to the current thread:
			setSubject(subjectUnderTest)
		}

		doAfter {
			clearSubject()
		}

		"handle logging in correctly" in {
			val user = testUser
				.save

			val authToken1 = new UsernamePasswordToken(testUser.email.is, "garbage")
			subject.login(authToken1) must throwA[IncorrectCredentialsException]

			val authToken2 = new UsernamePasswordToken("garbage", "garbage")
			subject.login(authToken2) must throwA[UnknownAccountException]

			val authToken = new UsernamePasswordToken(testUser.email.is, userPassword)
			subject.login(authToken)
			subject.isAuthenticated must_== true

			user.delete_!
		}

		"handle permissions correctly" in {
			val printerPrint = "printer:print"

			val user = testUser
				.permissions(printerPrint :: Nil)
				.save

			val authToken = new UsernamePasswordToken(testUser.email.is, userPassword)
			subject.login(authToken)

			subject.isPermitted("printer:print") must_== true
			subject.isPermitted("printer:manage") must_== false
			subject.isPermitted("printer:query") must_== false

			user.delete_!
		}

		"handle wildcard permissions correctly" in {
			val printerAll = "printer:*"

			val user = testUser
				.permissions(printerAll :: Nil)
				.save

			val authToken = new UsernamePasswordToken(testUser.email.is, userPassword)
			subject.login(authToken)

			subject.isPermitted("printer:print") must_== true
			subject.isPermitted("printer:manage") must_== true
			subject.isPermitted("printer:query") must_== true

			user.delete_!
		}

		"handle list permissions correctly" in {
			val printerSome = "printer:print,query"

			val user = testUser
				.permissions(printerSome :: Nil)
				.save

			val authToken = new UsernamePasswordToken(testUser.email.is, userPassword)
			subject.login(authToken)

			subject.isPermitted("printer:print") must_== true
			subject.isPermitted("printer:manage") must_== false
			subject.isPermitted("printer:query") must_== true

			user.delete_!
		}
	}
}
