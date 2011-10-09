package com.eltimn
package auth.mongo

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
  object permissions extends MongoListField[Role, String](this)
}
object Role extends Role with MongoMetaRecord[Role] {
  override def collectionName = "user.roles"
}
