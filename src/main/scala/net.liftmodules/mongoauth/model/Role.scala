package net.liftmodules.mongoauth
package model

import field.PermissionListField
import net.liftweb.common._
import net.liftweb.mongodb.record._
import net.liftweb.record.field.StringField
import net.liftweb.http.S
/*
 * Simple record for storing roles. Role name is the PK.
 */
class Role private () extends MongoRecord[Role] {
  def meta = Role

  object id extends StringField(this, 32) {
    override def name = "_id"
    override def displayName = S ? "liftmodule-monogoauth.role.id.displayName"
  }
  object permissions extends PermissionListField(this)

  override def equals(other: Any): Boolean = other match {
    case r: Role => r.id.get == this.id.get
    case _ => false
  }
}
object Role extends Role with MongoMetaRecord[Role] {
  override def collectionName = "user.roles"

  def findOrCreate(in: String): Role = find(in).openOr(createRecord.id(in))
  @deprecated("Use findOrCreateAndSaveBox instead.", "0.6")
  def findOrCreateAndSave(in: String): Role = findOrCreate(in).save(false)

  def findOrCreateAndSaveBox(in: String): Box[Role] = findOrCreate(in).saveBox()
}
