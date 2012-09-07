/*******************************************************************************
 * <copyright file="Role.scala">
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

import field.PermissionListField

import net.liftweb._
import mongodb.record._
import mongodb.record.field._
import record.field.StringField

import org.bson.types.ObjectId

/*
 * Simple record for storing roles. Role name is the PK.
 */
class Role private () extends MongoRecord[Role] {
  def meta = Role

  object id extends StringField(this, 32) {
    override def name = "_id"
    override def displayName = "Name"
  }
  object permissions extends PermissionListField(this)

  override def equals(other: Any): Boolean = other match {
    case r: Role => r.id.is == this.id.is
    case _ => false
  }
}
object Role extends Role with MongoMetaRecord[Role] {
  override def collectionName = "user.roles"

  def findOrCreate(in: String): Role = find(in).openOr(createRecord.id(in))
  def findOrCreateAndSave(in: String): Role = findOrCreate(in).save
}
