package com.eltimn.auth.mongo

import java.util.UUID

import org.joda.time.DateTime

import net.liftweb._
import common._
import http._
import mongodb.record._
import mongodb.record.field._
import record.MandatoryTypedField

import org.bson.types.ObjectId

trait AuthLoginToken {
  def url: String
}

trait AuthLoginTokenMeta[T <: AuthLoginToken, IdType] {

  //def logUserInFromToken(user: T): LiftResponse
  def createForUserId(uid: IdType): T
  def handleToken: LiftResponse
  def deleteAllByUserId(uid: IdType)

  //def findByAnyId(id: Any): Box[T]
}


trait ProtoLoginToken[T <: ProtoLoginToken[T]] extends MongoRecord[T] with AuthLoginToken {
  self: T =>

  def id: MandatoryTypedField[_]
  def userId: MandatoryTypedField[_]

  object expires extends DateField(this) {
    override def defaultValue = ((new DateTime).plusHours(48)).toDate
  }

  def url = S.hostAndPath+"/auth?token="+id.toString
}

/*
trait LoginTokenMeta[T <: ProtoLoginToken[T]] {
  self: T =>

  //def findAny(in: Any): Box[T]


}
*/

trait ProtoLoginTokenMeta[ModelType <: ProtoLoginToken[ModelType], IdType]
extends MongoMetaRecord[ModelType] with AuthLoginTokenMeta[ModelType, IdType]
{
  self: ModelType =>

  private lazy val userMeta = AuthRules.authUserMeta.vend
  private lazy val indexUrl = AuthRules.indexURL.vend.toString

  def findByStringId(in: String): Box[ModelType]

  def handleToken: LiftResponse = {
    var resp = RedirectResponse(indexUrl)
    S.param("token").flatMap(findByStringId) match {
      case Full(at) if ((new DateTime).getMillis >= (new DateTime(at.expires.value)).getMillis) => {
        S.error("Auth token has expired.")
        at.delete_!
      }
      case Full(at) => userMeta.logUserInFromToken(at.userId.toString) match {
        case Full(_) => {
          at.delete_!
          resp = RedirectResponse("/set_password") // need new transition page to allow user to select to set a password and/or to "remember me"
        }
        case _ => S.error("User not found.")
      }
      case _ => S.warning("Login token not provided.")
    }

    resp
  }
}

class ObjectIdLoginToken extends ProtoLoginToken[ObjectIdLoginToken] with ObjectIdPk[ObjectIdLoginToken] {
  def meta = ObjectIdLoginToken

  object userId extends ObjectIdField(this)
}
object ObjectIdLoginToken extends ObjectIdLoginToken with ProtoLoginTokenMeta[ObjectIdLoginToken, ObjectId] {
  override def collectionName = "user.logintokens"

  def createForUserId(uid: ObjectId): ObjectIdLoginToken = createRecord.userId(uid).save

  def findByStringId(id: String): Box[ObjectIdLoginToken] =
    if (ObjectId.isValid(id)) find(new ObjectId(id))
    else Empty

  def deleteAllByUserId(uid: ObjectId): Unit = delete(userId.name, uid)
}

class UUIDLoginToken extends ProtoLoginToken[UUIDLoginToken] with UUIDPk[UUIDLoginToken] {
  def meta = UUIDLoginToken

  object userId extends UUIDField(this)
}
object UUIDLoginToken extends UUIDLoginToken with ProtoLoginTokenMeta[UUIDLoginToken, UUID] {
  override def collectionName = "user.logintokens"

  def createForUserId(uid: UUID): UUIDLoginToken = createRecord.userId(uid).save

  def findByStringId(id: String): Box[UUIDLoginToken] =
    if (ObjectId.isValid(id)) find(new ObjectId(id))
    else Empty

  def deleteAllByUserId(uid: UUID): Unit = delete(userId.name, uid)
}


/*
class LoginToken extends MongoRecord[LoginToken] with ObjectIdPk[LoginToken] {
  def meta = LoginToken

  object userId extends ObjectIdRefField(this, User)
  object expires extends DateField(this) {
    override def defaultValue = ((new DateTime).plusHours(48)).toDate
  }

  def authLink = S.hostAndPath+"/auth?token="+id.toString
}
object LoginToken extends LoginToken with MongoMetaRecord[LoginToken] {
  override def collectionName = "user.logintokens"

  def createForUser(uid: ObjectId): LoginToken = {
    createRecord.userId(uid).save
  }

  def deleteAllByUser(userId: ObjectId) {
    delete("userId", userId)
  }
}
*/