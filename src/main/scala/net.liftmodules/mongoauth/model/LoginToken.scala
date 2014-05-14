package net.liftmodules.mongoauth
package model

import field.ExpiresField

import org.joda.time.Hours

import net.liftweb._
import common._
import http._
import mongodb.record._
import mongodb.record.field._
import record.MandatoryTypedField
import util.Helpers.tryo

import org.bson.types.ObjectId

/**
  * This is a token for automatically logging a user in
  */
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

  private lazy val loginTokenUrl = MongoAuth.loginTokenUrl.vend
  private lazy val loginTokenExpires = MongoAuth.loginTokenExpires.vend

  def url(inst: LoginToken): String = "%s%s?token=%s".format(S.hostAndPath, loginTokenUrl, inst.id.toString)

  @deprecated("use createForUserIdBox instead", "0.6")
  def createForUserId(uid: ObjectId): LoginToken = {
    createRecord.userId(uid).save(false)
  }

  def createForUserIdBox(uid: ObjectId): Box[LoginToken] = {
    createRecord.userId(uid).saveBox
  }

  @deprecated("use deleteAllByUserIdBox instead", "0.6")
  def deleteAllByUserId(uid: ObjectId): Unit = {
    delete(userId.name, uid)
  }

  def deleteAllByUserIdBox(uid: ObjectId): Box[Unit] = tryo {
    delete(userId.name, uid)
  }

  def findByStringId(in: String): Box[LoginToken] =
    if (ObjectId.isValid(in)) find(new ObjectId(in))
    else Failure("Invalid ObjectId: "+in)
}
