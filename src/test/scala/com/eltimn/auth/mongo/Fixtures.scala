package com.eltimn
package auth.mongo

import java.util.UUID

import org.apache.shiro._
import authc.AuthenticationInfo
import authz.AuthorizationInfo

import org.bson.types.ObjectId

import net.liftweb._
import common._
import mongodb.record._
import mongodb.record.field._

class CustomUser private () extends MongoAuthUser[CustomUser] with UUIDPk[CustomUser] {
  def meta = CustomUser

  lazy val authPermissions: Set[String] = Set.empty
  lazy val authRoles: Set[String] = Set.empty
  
}
object CustomUser extends CustomUser with ProtoAuthUserMeta[CustomUser, UUID] {
  def findPasswordForUser(login: String): Box[(UUID, String)] = Empty

  def createUser(username: String, email: String, password: String, permissions: List[String]): Box[CustomUser] = {
    val newUser = createRecord
      .save

    Full(newUser)
  }
}

class UltraCustomUser private () extends MongoAuthUser[UltraCustomUser] with UUIDPk[UltraCustomUser] {
  def meta = UltraCustomUser

  lazy val authPermissions: Set[String] = Set.empty
  lazy val authRoles: Set[String] = Set.empty
  
}
object UltraCustomUser extends UltraCustomUser with MongoMetaRecord[UltraCustomUser] with AuthUserMeta {
  def findAuthenticatioInfo(login: Any, realmName: String): Box[AuthenticationInfo] = Empty
  def findAuthorizationInfo(principal: Any): Box[AuthorizationInfo] = Empty

  def createUser(username: String, email: String, password: String, permissions: List[String]): Box[UltraCustomUser] = {
    val newUser = createRecord
      .save

    Full(newUser)
  }
}