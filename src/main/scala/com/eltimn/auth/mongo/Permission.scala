package com.eltimn.auth.mongo

import net.liftweb._
import record.{MetaRecord, Record}

/*
case class Permission(val domain: String, val actions: Set[String] = Set.empty, val entities: Set[String] = Set.empty) {
  def implies(p: Permission) = {
    if (p.domain == this.domain) {
      if (p.actions.contains(Permission.wildcardToken))
        true
      else if (this.actions.headOption.map(it => p.actions.contains(it)).getOrElse(false))
        if (p.entities.contains(Permission.wildcardToken))
          true
        else
          this.entities.headOption.map(it => p.entities.contains(it)).getOrElse(false)
      else
        false
    }
    else
      false
  }
}

object AllPermission extends Permission(Permission.wildcardToken)
object NoPermission extends Permission("")

object Permission {
  lazy val wildcardToken = AuthRules.permissionWilcardToken.vend
  lazy val partDivider = AuthRules.permissionPartDivider.vend
  lazy val subpartDivider = AuthRules.permissionSubpartDivider.vend

  def fromString(s: String): Permission = s.split(partDivider).toList match {
    case s :: Nil if (s == wildcardToken) => AllPermission
    case s :: Nil if (s.length == 0) => NoPermission
    case simple :: Nil => Permission(simple)
    case domain :: actions :: Nil => Permission(domain, actions.split(subpartDivider).toSet)
    case domain :: actions :: entities :: Nil =>
      Permission(domain, actions.split(subpartDivider).toSet, entities.split(subpartDivider).toSet)
    case Nil => NoPermission
  }
}

trait PermissionMaker {
  def domain: String
  def actions: Set[String]
  def entities: Set[String]

  def make: Permission = Permission(domain, actions, entities)
  def makeAll: Permission = Permission(domain, Set(Permission.wildcardToken), entities)
}

class PermissionBuilder(val domain: String, val actions: Set[String], val entities: Set[String])
  extends PermissionMaker

abstract class EnumPermissionBuilder(
  val domain: String,
  val actions: Set[Enumeration#Value],
  val entities: Set[String],
  val availableActions: Enumeration
)
  extends PermissionMaker
{

  override def make: Permission = Permission(domain, actions.map(_.toString), entities)

  def makeAction(act: Enumeration#Value, entities: Set[String] = Set.empty): Permission =
    Permission(domain, Set(act.toString), entities)

  def addAction(act: Enumeration#Value): EnumPermissionBuilder = new EnumPermissionBuilder(domain, actions, entities, availableActions) {}
}

object CrudOperation extends Enumeration {
  type CrudOperation = Value
  val Create, Read, Update, Delete = Value
}

class CrudPermissionBuilder(domain: String)
  extends EnumPermissionBuilder(domain, Set.empty, Set.empty, CrudOperation)

*/

//case class RecordPermission[BaseRecord <: Record[BaseRecord]](val domain: MetaRecord[BaseRecord])

/*
sealed trait Permission {
  def implies(p: Permission): Boolean
}

object Permission {
  lazy val wildcardToken = AuthRules.permissionWilcardToken.vend
  lazy val partDivider = AuthRules.permissionPartDivider.vend
  lazy val subpartDivider = AuthRules.permissionSubpartDivider.vend

  def fromString(s: String): Permission = s.split(partDivider).toList match {
    case s :: Nil if (s == wildcardToken) => AllPermission
    case s :: Nil if (s.length == 0) => NoPermission
    //case dom :: Nil => DomainPermission(dom)
    //case dom :: acts :: Nil => DomainPermission(dom, acts)
    case dom :: acts :: ents :: Nil => DomainPermission(dom, acts, ents)
    case _ => NoPermission
  }
}

object AllPermission extends Permission {
  def implies(p: Permission) = true
  override def toString = Permission.wildcardToken
}

object NoPermission extends Permission {
  def implies(p: Permission) = false
  override def toString = ""
}
*/
/*
 * A simple permission whereby there is only one part. A user either has permission or they don't.

case class SimplePermission(val permission: String) extends Permission {
  def implies(p: Permission) = p match {
    case AllPermission => true
    case NoPermission => false
    case SimplePermission(sp) => sp == this.permission
    case _ => false
  }
}
*/

/*
 * A permission that has three parts; domain, actions, and entities
 */
case class Permission(
  val domain: String,
  val actions: Set[String] = Set(Permission.wildcardToken),
  val entities: Set[String] = Set(Permission.wildcardToken))
{
  def implies(p: Permission) = p match {
    case Permission(d, a, e) =>
      if (d == Permission.wildcardToken)
        true
      else if (d == this.domain) {
        if (a.contains(Permission.wildcardToken))
          true
        else if (this.actions.headOption.map(it => a.contains(it)).getOrElse(false))
          if (e.contains(Permission.wildcardToken))
            true
          else
            this.entities.headOption.map(it => e.contains(it)).getOrElse(false)
        else
          false
      }
      else
        false
    case _ => false
  }

  def implies(ps: Set[Permission]): Boolean = ps.exists(this.implies)

  override def toString = {
    domain+Permission.partDivider+actions.mkString(Permission.subpartDivider)+Permission.partDivider+entities.mkString(Permission.subpartDivider)
  }
}

object Permission {
  lazy val wildcardToken = AuthRules.permissionWilcardToken.vend
  lazy val partDivider = AuthRules.permissionPartDivider.vend
  lazy val subpartDivider = AuthRules.permissionSubpartDivider.vend

  def apply(domain: String, actions: String): Permission =
    apply(domain, actions.split(Permission.subpartDivider).toSet)

  def apply(domain: String, actions: String, entities: String): Permission =
    apply(domain, actions.split(Permission.subpartDivider).toSet, entities.split(Permission.subpartDivider).toSet)

  def fromString(s: String): Permission = s.split(partDivider).toList match {
    case s :: Nil if (s == wildcardToken) => all
    case s :: Nil if (s.length == 0) => none
    case dom :: Nil => Permission(dom)
    case dom :: acts :: Nil => Permission(dom, acts)
    case dom :: acts :: ents :: Nil => Permission(dom, acts, ents)
    case _ => none
  }

  lazy val all = Permission(wildcardToken)
  lazy val none = Permission("")
}

/*
 * A permission that has three parts; domain, actions, and entities

case class EntityPermission(val domain: String, val actions: Set[String], val entities: Set[String])
  extends Permission
{
  def implies(p: Permission) = p match {
    case AllPermission => true
    case NoPermission => false
    case EntityPermission(d, a, e) =>
      if (d == this.domain) {
        if (a.contains(Permission.wildcardToken))
          true
        else if (this.actions.headOption.map(it => a.contains(it)).getOrElse(false))
          if (e.contains(Permission.wildcardToken))
            true
          else
            this.entities.headOption.map(it => e.contains(it)).getOrElse(false)
        else
          false
      }
      else
        false
    case _ => false
  }
}

object EntityPermission {
  def apply(domain: String, actions: String, entities: String): EntityPermission =
    apply(domain, actions.split(Permission.subpartDivider).toSet, entities.split(Permission.subpartDivider).toSet)
}
*/

/*
case class WildcardPermission(
  val wildcardString: String,
  val caseSensitive: Boolean = AuthRules.permissionCaseSensitive.vend
) extends Permission {

  if (wildcardString.trim.length == 0) {
    throw new IllegalArgumentException("Wildcard string cannot be empty. Make sure permission strings are properly formatted.")
  }

  val parts: List[Set[String]] = {

    val mainparts = wildcardString.trim.split(AuthRules.permissionPartDivider.vend)
      .map(p => {
        val subparts = p.split(AuthRules.permissionSubpartDivider.vend).map(sp => {
          if (!caseSensitive) {
            sp.toLowerCase
          }
          else
            sp
        })

        if (subparts.isEmpty) {
          throw new IllegalArgumentException("Wildcard string cannot contain parts with only dividers. Make sure permission strings are properly formatted.")
        }

        subparts.toSet
      }).toList

    if (mainparts.isEmpty) {
      throw new IllegalArgumentException("Wildcard string cannot contain only dividers. Make sure permission strings are properly formatted.")
    }

    mainparts
  }


  //private val permissionWilcardToken = AuthRules.permissionWilcardToken.vend
  //private val permissionPartDivider = AuthRules.permissionPartDivider.vend
  //private val permissionSubpartDivider = AuthRules.permissionSubpartDivider.vend
  //private val permissionCaseSensitive = AuthRules.permissionCaseSensitive.vend

  def implies(p: Permission) = p match {
    case wp: WildcardPermission => {
      var i = 0
      wp.parts.foreach { op =>
        if (parts.size -1 < i)
          true
        else {
          val part = parts(i)
        }
      }
    }
    case _ => false // By default only supports comparisons with other WildcardPermissions
  }

  private def is
}
*/