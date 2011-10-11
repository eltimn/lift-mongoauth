package com.eltimn.auth.mongo

import java.util.UUID
import scala.xml.{NodeSeq, Text}

import org.bson.types.ObjectId

import net.liftweb._
import common._
import http.{CleanRequestVarOnSessionTransition, RequestVar, S, SessionVar}
import mongodb.record._
import mongodb.record.field._
import record.MandatoryTypedField
import record.field.{PasswordField => _, _}
import util.FieldError
import util.Helpers

import com.mongodb.QueryBuilder

/**
 * AuthUser is a base class that gives you a "User" that has roles and permissions.
 */
trait AuthUser {
  /*
   * String representing the User ID
   */
  def userIdAsString: String

  /*
   * A list of this user's permissions
   */
  def authPermissions: Set[String]

  /*
   * A list of this user's roles
   */
  def authRoles: Set[String]
}


trait AuthUserMeta[UserType <: AuthUser, IdType] {
  //def findAuthenticatioInfo(login: Any, realmName: String): Box[AuthenticationInfo]
  //def findAuthorizationInfo(principal: Any): Box[AuthorizationInfo]

  /*
   * Always true when the user request var is defined.
   */
  def isLoggedIn: Boolean
  /*
   * User logged in by supplying password. False if auto logged in by ExtSession or LoginToken.
   */
  def isAuthenticated: Boolean
  /*
   * Current user has the given role
   */
  def hasRole(role: String): Boolean
  def lacksRole(role: String): Boolean = !hasRole(role)
  def hasAnyRoles(roles: Seq[String]) = roles exists (r => hasRole(r.trim))

  /*
   * Current user has the given permission
   */
  def hasPermission(permission: String): Boolean
  def lacksPermission(permission: String): Boolean = !hasPermission(permission)

  /*
   * Log the current user out
   */
  def logUserOut(): Unit

  /*
   * Log user in when using a LoginToken
   */
  def logUserInFromToken(id: String): Box[Unit] = Empty

  /*
   * The LoginTokenMeta used, if any
   */
  def loginTokenMeta: Box[AuthLoginTokenMeta[_, IdType]] = Empty
}

/*
 * Trait that has login related code
 */
trait UserLifeCycle[UserType <: AuthUser] {
  protected lazy val siteName = AuthRules.siteName.vend
  protected lazy val sysEmail = AuthRules.systemEmail.vend
  protected lazy val sysUsername = AuthRules.systemUsername.vend

   /*
   * Given a String representing the User ID, find the user
   */
  def findByStringId(id: String): Box[UserType]

  // log in/out lifecycle callbacks
  def onLogIn: List[UserType => Unit] = Nil
  def onLogOut: List[Box[UserType] => Unit] = Nil

  // current userId stored in the session.
  private object curUserId extends SessionVar[Box[String]](Empty)
  def currentUserId: Box[String] = curUserId.is

  private object curUserIsAuthenticated extends SessionVar[Boolean](false)

  // Request var that holds the User instance
  private object curUser extends RequestVar[Box[UserType]](currentUserId.flatMap(findByStringId))
  with CleanRequestVarOnSessionTransition {
    override lazy val __nameSalt = Helpers.nextFuncName
  }
  def currentUser: Box[UserType] = curUser.is

  def isLoggedIn: Boolean = currentUserId.isDefined
  def isAuthenticated: Boolean = curUserIsAuthenticated.is

  def hasRole(role: String): Boolean = currentUser.map(_.authRoles.exists(_ == role)).openOr(false)
  /* TODO: Write this so it behaves like Shiro's WildCardPermission */
  def hasPermission(permission: String): Boolean = currentUser.map(_.authPermissions.exists(_ == permission)).openOr(false)

  def logUserIdIn(id: String) {
    curUser.remove()
    curUserId(Full(id))
  }

  def logUserIn(who: UserType, isAuthed: Boolean) {
    curUserId.remove()
    curUserIsAuthenticated.remove()
    curUser.remove()
    curUserId(Full(who.userIdAsString))
    curUserIsAuthenticated(isAuthed)
    curUser(Full(who))
    onLogIn.foreach(_(who))
  }

  def logUserOut() {
    onLogOut.foreach(_(currentUser))
    curUserId.remove()
    curUserIsAuthenticated.remove()
    curUser.remove()
    S.session.foreach(_.destroySession())
  }
}


/*
 * Mongo version of AuthUser
 */
trait MongoAuthUser[T <: MongoAuthUser[T]] extends MongoRecord[T] with AuthUser {
  self: T =>

  def id: MandatoryTypedField[_]
  def email: StringField[_]
  //def fancyEmail: String
}


/*
 * Mix this in for a simple user.
 */
trait ProtoAuthUser[T <: ProtoAuthUser[T]] extends MongoAuthUser[T] {
  self: T =>

  import Helpers._

  def isRegistered: Boolean

  object username extends StringField(this, 32) {
    override def displayName = "Username"
    override def setFilter = trim _ :: super.setFilter

    private def valUnique(msg: => String)(value: String): List[FieldError] = {
      if (value.length > 0)
        meta.findAll(name, value).filterNot(_.id.is == owner.id.is).map(u =>
          FieldError(this, Text(msg))
        )
      else
        Nil
    }

    override def validations =
      valUnique("Another user is already using that username, please enter a different one.") _ ::
      valMinLen(3, "Username must be at least 3 characters.") _ ::
      valMaxLen(32, "Username must be less than 33 characters.") _ ::
      super.validations
  }

  /*
  * http://www.dominicsayers.com/isemail/
  */
  object email extends EmailField(this, 254) {
    override def displayName = "Email"
    override def setFilter = trim _ :: toLower _ :: super.setFilter

    private def valUnique(msg: => String)(value: String): List[FieldError] = {
      owner.meta.findAll(name, value).filter(_.id.is != owner.id.is).map(u =>
        FieldError(this, Text(msg))
      )
    }

    override def validations =
      valUnique("That email address is already registered with us.") _  ::
      valMaxLen(254, "Email must be 254 characters or less.") _ ::
      super.validations
  }
  // email address has been verified by clicking on a LoginToken link
  object verified extends BooleanField(this)
  object password extends PasswordField(this, 32, Full(confirmPassword)) {
    override def displayName = "Password"
    override def shouldDisplay_? = !isRegistered
  }
  object confirmPassword extends StringField(this, 32) {
    override def ignoreField_? = true
    override def displayName = "Confirm Password"

    private def elem = S.fmapFunc(S.SFuncHolder(this.setFromAny(_))) {
      funcName =>
        <input type="password" maxlength={maxLength.toString}
          name={funcName}
          value={valueBox openOr ""}
          tabindex={tabIndex toString}/>
    }

    override def toForm: Box[NodeSeq] =
      uniqueFieldId match {
        case Full(id) => Full(elem % ("id" -> id))
        case _ => Full(elem)
      }
  }
  object permissions extends MongoListField[T, String](this) {
    override def shouldDisplay_? = false
  }
  object roles extends ObjectIdRefListField(this, Role) {
    override def shouldDisplay_? = false

    def permissions: List[String] = objs.flatMap(_.permissions.is)
    def names: List[String] = objs.map(_.id.is)
  }

  lazy val authPermissions: Set[String] = (permissions.is ::: roles.permissions).toSet
  lazy val authRoles: Set[String] = roles.names.toSet

  lazy val fancyEmail = AuthUtil.fancyEmail(username.is, email.is)
}

trait ProtoAuthUserMeta[UserType <: MongoAuthUser[UserType], IdType]
extends MongoMetaRecord[UserType] with AuthUserMeta[UserType, IdType] with UserLifeCycle[UserType] {
  self: UserType =>

  //import scala.collection.JavaConversions._

  // create a new user
  //def createUser(username: String, email: String, password: String, permissions: List[String]): Box[ModelType]

  //def loginTokenForUser(user: UserType): Box[AuthLoginToken] = Empty

  //def loginTokenForUserId(uid: IdType) = loginTokenMeta.map(ltm => ltm.createForUserId(uid))

  def deleteAllLoginTokens(uid: IdType): Unit = loginTokenMeta.foreach(ltm => ltm.deleteAllByUserId(uid))

  // send an email to the user with a link for authorization
  def sendAuthLink(user: UserType): Unit
  def sendAuthLink(email: String, token: AuthLoginToken): Unit = {
    import net.liftweb.util.Mailer._

    val msgTxt =
      """
        |Someone requested a link to change your password on the %s website.
        |
        |If you did not request this, you can safely ignore it. It will expire 48 hours from the time this message was sent.
        |
        |Follow the link below or copy and paste it into your internet browser.
        |
        |%s
        |
        |Thanks,
        |%s
      """.format(siteName, token.url, sysUsername).stripMargin

    sendMail(
      From(AuthRules.systemFancyEmail),
      Subject("%s Password Help".format(siteName)),
      To(email),
      PlainMailBodyType(msgTxt)
    )
  }
}

class SimpleUser extends ProtoAuthUser[SimpleUser] with ObjectIdPk[SimpleUser] {
  def meta = SimpleUser

  def isRegistered: Boolean = !id.is.isNew
  def userIdAsString: String = id.toString
}

object SimpleUser extends SimpleUser with ProtoAuthUserMeta[SimpleUser, ObjectId] {
  import mongodb.BsonDSL._

  override def collectionName = "user.users"

  ensureIndex((email.name -> 1), true)
  ensureIndex((username.name -> 1), true)

  //def findByEmail(eml: String): Box[SimpleUser] = find(email.name, eml)

  def findByStringId(id: String): Box[SimpleUser] =
    if (ObjectId.isValid(id)) find(new ObjectId(id))
    else Empty

  override def loginTokenMeta = Full(ObjectIdLoginToken)

  override def logUserInFromToken(id: String): Box[Unit] = findByStringId(id).map { user =>
    user.verified(true)
    user.save
    logUserIn(user, false)
  }

  def loginTokenForUserId(uid: ObjectId) = loginTokenMeta.map(ltm => ltm.createForUserId(uid))

  def sendAuthLink(user: SimpleUser): Unit = loginTokenForUserId(user.id.is).foreach { lt =>
    sendAuthLink(user.email.is, lt)
  }

  /*
  def createUser(username: String, email: String, password: String, permissions: List[String]): Box[SimpleUser] = {
    val newUser = createRecord
      .username(username)
      .email(email)
      .password(password, true)
      .permissions(permissions)
      .save

    Full(newUser)
  }
  */
}