package net.liftmodules.mongoauth
package model

import org.bson.types.ObjectId

import net.liftweb._
import common._
import http.{StringField => _, BooleanField => _, _}
import mongodb.record.field._
import util.Helpers

class SimpleUser extends ProtoAuthUser[SimpleUser] with ObjectIdPk[SimpleUser] {
  def meta = SimpleUser

  def userIdAsString: String = id.toString
}

object SimpleUser extends SimpleUser with ProtoAuthUserMeta[SimpleUser] with Loggable {
  import mongodb.BsonDSL._

  //ensureIndex((email.name -> 1), true)
  //ensureIndex((username.name -> 1), true)

  def findByStringId(id: String): Box[SimpleUser] =
    if (ObjectId.isValid(id)) find(new ObjectId(id))
    else Empty

  override def onLogOut: List[Box[SimpleUser] => Unit] = List(
    x => logger.debug("User.onLogOut called."),
    boxedUser => boxedUser.foreach { u =>
      ExtSession.deleteExtCookie()
    }
  )

  /*
   * MongoAuth vars
   */
  private lazy val siteName = MongoAuth.siteName.vend
  private lazy val sysUsername = MongoAuth.systemUsername.vend
  private lazy val indexUrl = MongoAuth.indexUrl.vend
  private lazy val registerUrl = MongoAuth.registerUrl.vend
  private lazy val loginTokenAfterUrl = MongoAuth.loginTokenAfterUrl.vend

  /*
   * LoginToken
   */
  override def handleLoginToken: Box[LiftResponse] = {
    val resp = S.param("token").flatMap(LoginToken.findByStringId) match {
      case Full(at) if (at.expires.isExpired) => {
        at.delete_!
        RedirectWithState(indexUrl, RedirectState(() => { S.error(S ? "liftmodule-monogoauth.simpleUser.handleLoginToken.expiredToken") }))
      }
      case Full(at) => find(at.userId.get).map(user => {
        if (user.validate.length == 0) {
          user.verified(true)
          user.save(false)
          logUserIn(user)
          at.delete_!
          RedirectResponse(loginTokenAfterUrl)
        }
        else {
          at.delete_!
          regUser(user)
          RedirectWithState(registerUrl, RedirectState(() => { S.notice(S ? "liftmodule-monogoauth.simpleUser.handleLoginToken.completeRegistration") }))
        }
      }).openOr(RedirectWithState(indexUrl, RedirectState(() => { S.error( S ? "liftmodule-monogoauth.simpleUser.handleLoginToken.userNotFound") })))
      case _ => RedirectWithState(indexUrl, RedirectState(() => { S.warning(S ? "liftmodule-monogoauth.simpleUser.handleLoginToken.noToken") }))
    }

    Full(resp)
  }

  // send an email to the user with a link for logging in
  def sendLoginToken(user: SimpleUser): Unit = {
    import net.liftweb.util.Mailer._

    LoginToken.createForUserIdBox(user.id.get).foreach { token =>

      val msgTxt = S ? ( "liftmodule-monogoauth.simpleUser.sendLoginToken.msg", siteName, token.url, sysUsername)

      sendMail(
        From(MongoAuth.systemFancyEmail),
        Subject(S ? ("liftmodule-monogoauth.simpleUser.sendLoginToken.subject",siteName)),
        To(user.fancyEmail),
        PlainMailBodyType(msgTxt)
      )
    }
  }

  /*
  * Test for active ExtSession.
  */
  def testForExtSession: Box[Req] => Unit = {
    ignoredReq => {
      logger.debug("ExtSession currentUserId: "+currentUserId.toString)
      if (currentUserId.isEmpty) {
        ExtSession.handleExtSession match {
          case Full(es) => find(es.userId.get).foreach { user => logUserIn(user, false) }
          case Failure(msg, _, _) => logger.warn("Error logging user in with ExtSession: %s".format(msg))
          case Empty => logger.warn("Unknown error logging user in with ExtSession: Empty")
        }
      }
    }
  }

  object regUser extends SessionVar[SimpleUser](currentUser openOr createRecord)
}
