/*******************************************************************************
 * <copyright file="LoginToken.scala">
 * Copyright (c) 2011 - 2012. Heirko
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
 * <lastUpdate>07/09/12 15:13</lastUpdate>
 ******************************************************************************/

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

  override val collectionName = "user.logintokens"

  ensureIndex((userId.name -> 1))

  private lazy val loginTokenUrl = MongoAuth.loginTokenUrl.vend
  private lazy val loginTokenExpires = MongoAuth.loginTokenExpires.vend

  def url(inst: LoginToken): String = "%s%s?token=%s".format(S.hostAndPath, loginTokenUrl, inst.id)

  def createForUserId(uid: ObjectId): LoginToken = {
    createRecord.userId(uid).save
  }

  def deleteAllByUserId(uid: ObjectId) {
    delete(userId.name, uid)
  }

  def findByStringId(in: String): Box[LoginToken] =
    if (ObjectId.isValid(in)) find(new ObjectId(in))
    else Failure("Invalid ObjectId: "+in)
}
