

/*******************************************************************************
 * <copyright file="AuthUser.scala">
 * Copyright (c) 2011 - 2013. Heirko
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Heirko and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Heirko
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Heirko.
 *
 * Heirko tout droit réservé -- Tout information contenue ici est la propriété
 * de la société Heirko.
 *
 * </copyright>
 *
 * <author>Alexandre Richonnier</author>
 * <lastUpdate>25/04/13 00:43</lastUpdate>
 ******************************************************************************/

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
  def authPermissions: Set[Permission]

  /*
   * A list of this user's roles
   */
  def authRoles: Set[String]
}

trait AuthUserMeta[UserType <: AuthUser] {
  /*
   * True when the user request var is defined.
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
  def hasPermission(permission: Permission): Boolean
  def lacksPermission(permission: Permission): Boolean = !hasPermission(permission)

  /*
   * Log the current user out
   */
  def logUserOut(): Unit

  /*
   * Handle a LoginToken. Called from Locs.loginTokenLocParams
   */
  def handleLoginToken(): Box[LiftResponse] = Empty
}

/*
 * Trait that has login related code
 */
trait UserLifeCycle[UserType <: AuthUser] {

  /*
   * Given a String representing the User ID, find the user
   */
  def findByStringId(id: String): Box[UserType]

  // log in/out lifecycle callbacks
  def onLogIn: List[UserType => Unit] = Nil
  def onCreation: List[(UserType,String) => Unit] = Nil
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

  /**
   * Be careful throw an exception if not set
   * @return
   */
  def currentUser_! : UserType = currentUser.openOrThrowException("UserSessionMustBeSet")


  def isLoggedIn: Boolean = currentUserId.isDefined
  def isAuthenticated: Boolean = curUserIsAuthenticated.is

  def hasRole(user : UserType, role: String): Boolean = user.authRoles.exists(_ == role)

  def hasPermission(user : UserType, permission: Permission): Boolean =
          permission.implies(user.authPermissions)

  def hasRole(role: String): Boolean =
    currentUser.map(hasRole(_, role))
    .openOr(false)

  def hasPermission(permission: Permission): Boolean =
    currentUser.map(hasPermission(_, permission))
      .openOr(false)

  def logUserIn(who: UserType, isAuthed: Boolean = false, isRemember: Boolean = false) {
    curUserId.remove()
    curUserIsAuthenticated.remove()
    curUser.remove()
    curUserId(Full(who.userIdAsString))
    curUserIsAuthenticated(isAuthed)
    curUser(Full(who))
    XSRFToken.setToken()
    onLogIn.foreach(_(who))
    if (isRemember)
      ExtSession.createExtSession(who.userIdAsString)
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
}

/*
 * Mix this in for a simple user.
 */
trait ProtoAuthUser[T <: ProtoAuthUser[T]] extends MongoAuthUser[T] {
  self: T =>

  import Helpers._

  object username extends StringField(this, 32) {
    override def displayName = S.?("ui_login_username")
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
      valUnique(S.?("base_user_err_username_duplicate")) _ ::
      valMinLen(3,  S.?("base_user_err_must_at_least_characters").format(displayName, 3)) _ ::
      valMaxLen(32, S.?("base_user_err_must_at_max_characters").format(displayName, 254)) _ ::
      super.validations
  }

  /*
  * http://www.dominicsayers.com/isemail/
  */
  object email extends EmailField(this, 254) {
    override def displayName = S.?("ui_login_email")
    override def setFilter = trim _ :: toLower _ :: super.setFilter

    private def valUnique(msg: => String)(value: String): List[FieldError] = {
      owner.meta.findAll(name, value).filter(_.id.is != owner.id.is).map(u =>
        FieldError(this, Text(msg))
      )
    }

    override def validations =
      valMinLen(3, S.?("base_user_err_must_at_least_characters").format(displayName, 3)) _ ::
      valMaxLen(254, S.?("base_user_err_must_at_max_characters").format(displayName, 254)) _ ::
      valUnique(S.?("base_user_err_email_duplicate")) _  ::
        super.validations
  }
  // email address has been verified by clicking on a LoginToken link
  object verified extends BooleanField(this) {
    override def displayName = "Verified"
  }
//  object password extends PasswordField(this, 6, 32) {
//    override def displayName = "Password"
//  }
  object permissions extends PermissionListField(this)
  object roles extends StringRefListField(this, Role) {
    def permissions: List[Permission] = objs.flatMap(_.permissions.is)
    def names: List[String] = objs.map(_.id.is)
  }

  lazy val authPermissions: Set[Permission] = (permissions.is ::: roles.permissions).toSet
  lazy val authRoles: Set[String] = roles.names.toSet

  lazy val fancyEmail = AuthUtil.fancyEmail(username.is, email.is)
}

trait ProtoAuthUserMeta[UserType <: MongoAuthUser[UserType]]
extends MongoMetaRecord[UserType] with AuthUserMeta[UserType]
with UserLifeCycle[UserType] {
  self: UserType =>
}

