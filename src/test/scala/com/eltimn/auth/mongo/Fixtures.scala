package com.eltimn
package auth.mongo

import java.util.UUID

import org.bson.types.ObjectId

import net.liftweb._
import common._
import mongodb.record._
import mongodb.record.field._
import record.field._
import util.Helpers

class CustomUser private () extends MongoAuthUser[CustomUser] with UUIDPk[CustomUser] {
  def meta = CustomUser

  object email extends EmailField(this, 254)

  lazy val authPermissions: Set[String] = Set.empty
  lazy val authRoles: Set[String] = Set.empty

  def userIdAsString: String = id.toString
}

object CustomUser extends CustomUser with ProtoAuthUserMeta[CustomUser, UUID] {
  /*
  def createUser(username: String, email: String, password: String, permissions: List[String]): Box[CustomUser] = {
    val newUser = createRecord
      .save

    Full(newUser)
  }
  */

  def findByStringId(id: String): Box[CustomUser] = Helpers
    .tryo(UUID.fromString(id)).flatMap(find(_))

  override def loginTokenMeta = Full(UUIDLoginToken)

  def loginTokenForUserId(uid: UUID) = loginTokenMeta.map(ltm => ltm.createForUserId(uid))

  def sendAuthLink(user: CustomUser): Unit = loginTokenForUserId(user.id.is).foreach { lt =>
    sendAuthLink(user.email.is, lt)
  }

  override def logUserInFromToken(id: String): Box[Unit] = findByStringId(id).map { user =>
    logUserIn(user, false)
  }
}


class UltraCustomUser private () extends MongoAuthUser[UltraCustomUser] with UUIDPk[UltraCustomUser] {
  def meta = UltraCustomUser

  object email extends EmailField(this, 254)

  lazy val authPermissions: Set[String] = Set.empty
  lazy val authRoles: Set[String] = Set.empty

  def userIdAsString: String = id.toString
}

object UltraCustomUser extends UltraCustomUser with MongoMetaRecord[UltraCustomUser] {
  def findByStringId(id: String): Box[UltraCustomUser] = Helpers
    .tryo(UUID.fromString(id)).flatMap(find(_))
}