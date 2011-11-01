package com.eltimn.auth.mongo

import net.liftweb._
import common._
import mongodb.record.field.MongoListField
import mongodb.record.BsonRecord

import com.mongodb._

/*
class PermissionListField[OwnerType <: BsonRecord[OwnerType]](rec: OwnerType)
  extends MongoListField[OwnerType, String](rec)
{
  lazy val permissions: List[Permission] = value.map(s => Permission.fromString(s))
}
*/

class PermissionListField[OwnerType <: BsonRecord[OwnerType]](rec: OwnerType)
  extends MongoListField[OwnerType, Permission](rec)
{
  import scala.collection.JavaConversions._

  override def asDBObject: DBObject = {
    val dbl = new BasicDBList
    value.foreach { v => dbl.add(v.toString)
      /*
      val dbo = BasicDBObjectBuilder.start
      val acts = {
        val actsDbl = new BasicDBList
        v.actions.foreach { act => actsDbl.add(act) }
        actsDbl
      }
      val ents = {
        val entsDbl = new BasicDBList
        v.entities.foreach { ent => entsDbl.add(ent) }
        entsDbl
      }
      dbo.add("dom", v.domain)
      if (acts.length > 0)
        dbo.add("acts", acts)
      if (ents.length > 0)
        dbo.add("ents", ents)
      dbl.add(dbo.get)
      */
    }
    dbl
  }

  override def setFromDBObject(dbo: DBObject): Box[List[Permission]] =
    setBox(Full(dbo.keySet.toList.map(k => {
      Permission.fromString(dbo.get(k.toString).asInstanceOf[String])
      /*
      val permDbo = dbo.get(k.toString).asInstanceOf[DBObject]

      val acts: Set[String] =
        if (permDbo.containsField("acts"))
          permDbo.get("acts").asInstanceOf[BasicDBList].toSet.asInstanceOf[Set[String]]
        else
          Set.empty

      val ents: Set[String] =
        if (permDbo.containsField("ents"))
          permDbo.get("ents").asInstanceOf[BasicDBList].toSet.asInstanceOf[Set[String]]
        else
          Set.empty

      Option(permDbo.get("dom").asInstanceOf[String]).map(dom =>
        Permission(dom, acts, ents)
      ).toList
      */
    })))
}
