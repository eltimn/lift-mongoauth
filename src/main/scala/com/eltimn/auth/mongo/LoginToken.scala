package com.eltimn.auth.mongo

//import java.util.UUID

import org.joda.time.Hours

import net.liftweb._
import common._
import http._
import mongodb.record._
import mongodb.record.field._
import record.MandatoryTypedField

import org.bson.types.ObjectId

class LoginToken extends MongoRecord[LoginToken] with ObjectIdPk[LoginToken] {
  def meta = LoginToken

  object userId extends ObjectIdField(this)
  object expires extends ExpiresField(this, meta.loginTokenExpires)

  def url: String = meta.url(this)
}

object LoginToken extends LoginToken with MongoMetaRecord[LoginToken] {
  import mongodb.BsonDSL._

  override def collectionName = "user.logintokens"

  ensureIndex((userId.name -> 1))

  private lazy val loginTokenUrl = AuthRules.loginTokenUrl.vend
  lazy val loginTokenExpires = AuthRules.loginTokenExpires.vend

  def url(inst: LoginToken): String = "%s%s?token=%s".format(S.hostAndPath, loginTokenUrl.toString, inst.id.toString)

  def createForUserId(uid: ObjectId): LoginToken = {
    createRecord.userId(uid).save
  }

  def deleteAllByUserId(uid: ObjectId) {
    delete(userId.name, uid)
  }

  def findByStringId(id: String): Box[LoginToken] =
    if (ObjectId.isValid(id)) find(new ObjectId(id))
    else Empty
}

/*
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
*/
/*
trait LoginTokenMeta[T <: ProtoLoginToken[T]] {
  self: T =>

  //def findAny(in: Any): Box[T]


}
*/

/*
trait ProtoLoginTokenMeta[ModelType <: ProtoLoginToken[ModelType], IdType]
extends MongoMetaRecord[ModelType] with AuthLoginTokenMeta[ModelType, IdType]
{
  self: ModelType =>

  private lazy val userMeta = AuthRules.authUserMeta.vend
  private lazy val indexUrl = AuthRules.indexURL.vend
  private lazy val setPasswordUrl = AuthRules.setPasswordURL.vend

  def findByStringId(in: String): Box[ModelType]

  def handleToken: LiftResponse = {
    var respUrl = indexUrl.toString
    S.param("token").flatMap(findByStringId) match {
      case Full(at) if ((new DateTime).getMillis >= (new DateTime(at.expires.value)).getMillis) => {
        S.error("Login token has expired.")
        at.delete_!
      }
      case Full(at) => userMeta.logUserInFromToken(at.userId.toString) match {
        case Full(_) => {
          at.delete_!
          respUrl = setPasswordUrl.toString
        }
        case _ => S.error("User not found.")
      }
      case _ => S.warning("Login token not provided.")
    }

    RedirectResponse(respUrl)
  }
}

class SimpleLoginToken extends ProtoLoginToken[SimpleLoginToken] with ObjectIdPk[SimpleLoginToken] {
  def meta = SimpleLoginToken

  object userId extends ObjectIdField(this)
}
object SimpleLoginToken extends SimpleLoginToken with ProtoLoginTokenMeta[SimpleLoginToken, ObjectId] {
  import mongodb.BsonDSL._

  override def collectionName = "user.logintokens"

  ensureIndex((userId.name -> 1))

  def createForUserId(uid: ObjectId): SimpleLoginToken = createRecord.userId(uid).save

  def findByStringId(id: String): Box[SimpleLoginToken] =
    if (ObjectId.isValid(id)) find(new ObjectId(id))
    else Empty

  def deleteAllByUserId(uid: ObjectId): Unit = delete(userId.name, uid)
}
*/
/*
class UUIDLoginToken extends ProtoLoginToken[UUIDLoginToken] with UUIDPk[UUIDLoginToken] {
  def meta = UUIDLoginToken

  object userId extends UUIDField(this)
}
object UUIDLoginToken extends UUIDLoginToken with ProtoLoginTokenMeta[UUIDLoginToken, UUID] {
  override def collectionName = "user.logintokens"

  ensureIndex((userId.name -> 1))

  def createForUserId(uid: UUID): UUIDLoginToken = createRecord.userId(uid).save

  def findByStringId(id: String): Box[UUIDLoginToken] =
    if (ObjectId.isValid(id)) find(new ObjectId(id))
    else Empty

  def deleteAllByUserId(uid: UUID): Unit = delete(userId.name, uid)
}
*/
