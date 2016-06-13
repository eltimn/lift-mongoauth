package net.liftmodules.mongoauth

import field._
import model._

import scala.xml.{NodeSeq, Text}

import org.bson.types.ObjectId

import net.liftweb._
import common._
import http.{CleanRequestVarOnSessionTransition, LiftResponse, RequestVar, S, SessionVar}
import mongodb.record._
import mongodb.record.field._
import record.MandatoryTypedField
import record.field.{PasswordField => _, _}
import util.FieldError
import util.Helpers

trait LifeCycleUser {
  /*
   * String representing the User ID
   */
  def userIdAsString: String
}

/**
 * AuthUser is a base class that gives you a "User" that has roles and permissions.
 */
trait AuthUser extends LifeCycleUser {

  /*
   * A list of this user's permissions
   */
  def authPermissions: Set[Permission]

  /*
   * A list of this user's roles
   */
  def authRoles: Set[String]
}

/*
 * Trait that has login related code
 */
trait UserLifeCycle[UserType <: LifeCycleUser] {

  /**
    * Given a String representing the User ID, find the user
    */
  def findByStringId(id: String): Box[UserType]

  // log in/out lifecycle callbacks
  def onLogIn: List[UserType => Unit] = Nil
  def onLogOut: List[Box[UserType] => Unit] = Nil

  // current userId stored in the session.
  private object curUserId extends SessionVar[Box[String]](Empty)
  def currentUserId: Box[String] = curUserId.get

  private object curUserIsAuthenticated extends SessionVar[Boolean](false)

  // Request var that holds the User instance
  private object curUser extends RequestVar[Box[UserType]](currentUserId.flatMap(findByStringId))
  with CleanRequestVarOnSessionTransition {
    override lazy val __nameSalt = Helpers.nextFuncName
  }
  def currentUser: Box[UserType] = curUser.get

  /**
    * True when the user request var is defined.
    */
  def isLoggedIn: Boolean = currentUserId.isDefined

  /**
    * true if user logged in by supplying password. False if
    * auto logged in by ExtSession or LoginToken.
    */
  def isAuthenticated: Boolean = curUserIsAuthenticated.is

  def logUserIn(who: UserType, isAuthed: Boolean = false, isRemember: Boolean = false): Unit = {
    curUserId.remove()
    curUserIsAuthenticated.remove()
    curUser.remove()
    curUserId(Full(who.userIdAsString))
    curUserIsAuthenticated(isAuthed)
    curUser(Full(who))
    onLogIn.foreach(_(who))
    if (isRemember)
      ExtSession.createExtSessionBox(who.userIdAsString)
  }

  /**
    * Log the current user out
    */
  def logUserOut(): Unit = {
    onLogOut.foreach(_(currentUser))
    curUserId.remove()
    curUserIsAuthenticated.remove()
    curUser.remove()
    S.session.foreach(_.destroySession())
  }

  /**
    * Handle a LoginToken. Called from Locs.loginTokenLocParams
    */
  def handleLoginToken(): Box[LiftResponse] = Empty
}

trait AuthUserMeta[UserType <: AuthUser] extends UserLifeCycle[UserType] {

  /**
    * Current user has the given role
    */
  def hasRole(role: String): Boolean = currentUser
    .map(_.authRoles.exists(_ == role))
    .openOr(false)

  def lacksRole(role: String): Boolean = !hasRole(role)
  def hasAnyRoles(roles: Seq[String]) = roles.exists(r => hasRole(r.trim))

  /*
   * Current user has the given permission
   */
  def hasPermission(permission: Permission): Boolean = {
    currentUser
      .map(u => permission.implies(u.authPermissions))
      .openOr(false)
  }

  def lacksPermission(permission: Permission): Boolean = !hasPermission(permission)
}

/**
  * Mongo version of AuthUser
  */
trait MongoAuthUser[T <: MongoAuthUser[T]] extends MongoRecord[T] with AuthUser {
  self: T =>

  def id: MandatoryTypedField[_]
  def email: StringField[_]
}

/*
 * Mix this in for a simple user.
 */
trait ProtoAuthUser[T <: ProtoAuthUser[T]] extends MongoAuthUser[T] {
  self: T =>

  import Helpers._

  object username extends StringField(this, 32) {
    override def displayName = "Username"
    override def setFilter = trim _ :: super.setFilter

    private def valUnique(msg: => String)(value: String): List[FieldError] = {
      if (value.length > 0)
        meta.findAll(name, value).filterNot(_.id.get == owner.id.get).map(u =>
          FieldError(this, Text(msg))
        )
      else
        Nil
    }

    override def validations =
      valUnique(S ? "liftmodule-monogoauth.monogoAuthUser.username.validation.unique") _ ::
      valMinLen(3, S ? "liftmodule-monogoauth.monogoAuthUser.username.validation.min.length") _ ::
      valMaxLen(32, S ? "liftmodule-monogoauth.monogoAuthUser.username.validation.max.length") _ ::
      super.validations
  }

  /*
  * http://www.dominicsayers.com/isemail/
  */
  object email extends EmailField(this, 254) {
    override def displayName = "Email"
    override def setFilter = trim _ :: toLower _ :: super.setFilter

    private def valUnique(msg: => String)(value: String): List[FieldError] = {
      owner.meta.findAll(name, value).filter(_.id.get != owner.id.get).map(u =>
        FieldError(this, Text(msg))
      )
    }

    override def validations =
      valUnique("That email address is already registered with us") _  ::
      valMaxLen(254, "Email must be 254 characters or less") _ ::
      super.validations
  }
  // email address has been verified by clicking on a LoginToken link
  object verified extends BooleanField(this) {
    override def displayName = "Verified"
  }
  object password extends PasswordField(this, 6, 32) {
    override def displayName = "Password"
  }
  object permissions extends PermissionListField(this)
  object roles extends StringRefListField(this, Role) {
    def permissions: List[Permission] = objs.flatMap(_.permissions.get)
    def names: List[String] = objs.map(_.id.get)
  }

  lazy val authPermissions: Set[Permission] = (permissions.get ::: roles.permissions).toSet
  lazy val authRoles: Set[String] = roles.names.toSet

  lazy val fancyEmail = AuthUtil.fancyEmail(username.get, email.get)
}

trait ProtoAuthUserMeta[UserType <: MongoAuthUser[UserType]]
extends MongoMetaRecord[UserType] with AuthUserMeta[UserType]
with UserLifeCycle[UserType] {
  self: UserType =>
}

