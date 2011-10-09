package com.eltimn
package auth.mongo

import java.util.UUID
import scala.xml.{NodeSeq, Text}

import org.apache.shiro._
import authc.{AuthenticationInfo, SimpleAuthenticationInfo}
import authz.{AuthorizationInfo, SimpleAuthorizationInfo}

import org.bson.types.ObjectId

import net.liftweb._
import common._
import http.{RequestVar, S}
import mongodb.record._
import mongodb.record.field._
import net.liftweb.record.MandatoryTypedField
import net.liftweb.record.field.{PasswordField => _, _}
import net.liftweb.util.FieldError
import net.liftweb.util.Helpers._

import com.mongodb.{QueryBuilder}

/**
 * AuthUser is a base class that gives you a "User" that has roles and permissions for using with Shiro.
 */
trait AuthUser {
  def authPermissions: Set[String]
  def authRoles: Set[String]
}

trait AuthUserMeta {
  def findAuthenticatioInfo(login: Any, realmName: String): Box[AuthenticationInfo]
  def findAuthorizationInfo(principal: Any): Box[AuthorizationInfo]
}

/*
 * Mongo version of AuthUser
 */
trait MongoAuthUser[T <: MongoAuthUser[T]] extends MongoRecord[T] with AuthUser {
  self: T =>

  def id: MandatoryTypedField[_]
  //def email: MandatoryTypedField[_]
  //def fancyEmail: String
}


/*
 * Mix this in for a simple user.
 */
trait ProtoAuthUser[T <: ProtoAuthUser[T]] extends MongoAuthUser[T] {
  self: T =>

  def isRegistered: Boolean

  //val displayNames: Map[String, String] = Map.empty

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

  lazy val fancyEmail = username.is+" <"+email.is+">"
}

trait ProtoAuthUserMeta[ModelType <: MongoAuthUser[ModelType], IdType]
extends MongoMetaRecord[ModelType] with AuthUserMeta
{
  self: ModelType =>

  import scala.collection.JavaConversions._

  // create a new user
  def createUser(username: String, email: String, password: String, permissions: List[String]): Box[ModelType]

  // current userId stored by Shiro. This is stored in a session that's managed by Shiro.
  def currentUserId: Box[IdType] = shiro.Utils.principal[IdType]

  // Request var that holds the User instance
  private object currentUserVar extends RequestVar[Box[ModelType]](currentUserId.flatMap(findAny(_)))
  def currentUser: Box[ModelType] = currentUserVar.is

  def findAuthenticatioInfo(login: Any, realmName: String): Box[AuthenticationInfo] =
    (Box !! login.asInstanceOf[String])
      //.flatMap(findByLogin(_))
      .flatMap(findPasswordForUser(_))
      .map { case (id, pwd) =>
        new SimpleAuthenticationInfo(id, pwd, realmName)
      }

  def findAuthorizationInfo(userId: Any): Box[AuthorizationInfo] = {
    val idToFind = userId.asInstanceOf[IdType]
    val user =
      if (currentUserVar.set_? && currentUserVar.is.map(_.id.is).exists(_ == id))
        currentUserVar.is
      else
        findAny(idToFind)

    user.map { u =>
      val info = new SimpleAuthorizationInfo(u.authRoles)
      info.setStringPermissions(u.authPermissions)
      info
    }
  }

  // Get a user's password and id
  def findPasswordForUser(login: String): Box[(IdType, String)]

  // helper method for fulfilling findPasswordForUser(login: String) method.
  def findPasswordForUser(login: String, queryField: String, passwordField: String): Box[(IdType, String)] = {
    useColl { coll =>
      val qry = QueryBuilder.start(queryField).is(login).get
      val flds = QueryBuilder.start(passwordField).is(1).get
      tryo(coll.findOne(qry, flds)).flatMap(Box !! _)
        .filter(dbo => dbo.containsField(passwordField) && dbo.containsField("_id"))
        .map(dbo => (dbo.get("_id").asInstanceOf[IdType], dbo.get(passwordField).asInstanceOf[String]))
    }
  }
}

class SimpleUser extends ProtoAuthUser[SimpleUser] with ObjectIdPk[SimpleUser] {
  def meta = SimpleUser

  def isRegistered: Boolean = !id.is.isNew
}

object SimpleUser extends SimpleUser with ProtoAuthUserMeta[SimpleUser, ObjectId] {
  import mongodb.BsonDSL._

  override def collectionName = "user.users"

  ensureIndex((email.name -> 1), true)
  ensureIndex((username.name -> 1), true)

  def findPasswordForUser(login: String): Box[(ObjectId, String)] =
    findPasswordForUser(login, email.name, password.name) or findPasswordForUser(login, username.name, password.name)

  def createUser(username: String, email: String, password: String, permissions: List[String]): Box[SimpleUser] = {
    val newUser = createRecord
      .username(username)
      .email(email)
      .password(password, true)
      .permissions(permissions)
      .save

    Full(newUser)
  }

  /*
  val siteName = "Example Site"
  val systemEmail = "info@example.com"
  val systemUser: SimpleUser = find("email", systemEmail) openOr {
    createRecord
      .username("%s Staff".format(siteName))
      .email(systemEmail)
      .verified(true)
      .password("example", true)
      .save
  }

  // send an email to the user with a link for authorization
  def sendAuthLink(user: SimpleUser) {
    import net.liftweb.util.Mailer._

    val authToken = SimpleAuthToken.createForUser(user.id.is)

    val msgTxt =
      """
        |Someone requested a link to change your password on the %s website.
        |
        |If you did not request this, you can safely ignore it. It will expire 48 hours from the time this message was sent.
        |
        |Follow the link below or copy and paste it into your internet browser.
      """+authToken.authLink+"""
        |
        |Thanks,
        |%s Staff
      """.format(siteName, siteName).stripMargin

    sendMail(
      From(systemUser.fancyEmail),
      Subject("%s Password Help".format(siteName)),
      To(user.email.is),
      PlainMailBodyType(msgTxt)
    )
  }
  */
}