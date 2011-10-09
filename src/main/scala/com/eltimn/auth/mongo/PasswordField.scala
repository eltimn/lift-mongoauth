package com.eltimn
package auth.mongo

import java.util.regex._
import scala.xml._

import org.mindrot.jbcrypt.BCrypt

import net.liftweb._
import common._
import http.{Factory, S}
import http.js._
import json.JsonAST.{JNothing, JNull, JString, JValue}
import mongodb.record.field._
import mongodb.record._
import net.liftweb.record._
import net.liftweb.record.field._
import util._
import Helpers._
import S._
import JE._

object PasswordField extends Factory {

  val minPasswordLength = new FactoryMaker[Int](6) {}
  val logRounds = new FactoryMaker[Int](10) {}

  private[auth] def hashpw(in: String): Box[String] =
    tryo(BCrypt.hashpw(in, BCrypt.gensalt(logRounds.vend)))
    
  def isMatch(toTest: String, encryptedPassword: String): Boolean = tryo(BCrypt.checkpw(toTest, encryptedPassword)).openOr(false)
}

trait PasswordTypedField extends TypedField[String] {
  def maxLength: Int
  def confirmField: Box[TypedField[String]]

  /*
   * Call this after validation and before it is saved to the db to hash
   * the password. Eg. in the finish method of a screen.
   */
  def hashIt {
    valueBox foreach { v =>
      setBox(PasswordField.hashpw(v))
    }
  }

  /*
   * jBCrypt throws "String index out of range" exception
   * if password is an empty String
   */
  def isMatch(toTest: String): Boolean = valueBox
    .filter(_.length > 0)
    .map(p => PasswordField.isMatch(toTest, p))
    .openOr(false)

  def elem = S.fmapFunc(SFuncHolder(this.setFromAny(_))) {
    funcName => <input type="password" maxlength={maxLength.toString}
      name={funcName}
      value={valueBox openOr ""}
      tabindex={tabIndex toString}/>}

  override def toForm: Box[NodeSeq] =
    uniqueFieldId match {
      case Full(id) => Full(elem % ("id" -> id))
      case _ => Full(elem)
    }
}

class PasswordField[OwnerType <: Record[OwnerType]](
  rec: OwnerType, maxLength: Int, val confirmField: Box[TypedField[String]]
)
extends StringField[OwnerType](rec, maxLength) with PasswordTypedField {

  val minLength = PasswordField.minPasswordLength.vend
  
  /*
   * If confirmField is Full, check it against the inputted value.
   */
  private def valMatch(msg: => String)(value: String): List[FieldError] = {
    confirmField.filterNot(_.get == value).map(p =>
	    FieldError(this, Text(msg))
	  ).toList
	}
  
  override def validations =
    valMatch("Passwords must match.") _ ::
		valMinLen(minLength, "Password must be at least "+minLength+" characters.") _ ::
		valMaxLen(maxLength, "Password must be "+maxLength+" characters or less.") _ ::
		super.validations

	override def setFilter = trim _ :: super.setFilter
	
	/*
	 * Use this when creating users programatically. It allows chaining:
	 * User.createRecord.email("test@domain.com").password("pass1", true).save
	 * This will automatically hash the plain text password if isPlain is
	 * true.
	 */
	def apply(in: String, isPlain: Boolean): OwnerType = {
    val hashed =
      if (isPlain)
        PasswordField.hashpw(in) openOr ""
      else
        in
    if (owner.meta.mutable_?) {
      this.setBox(Full(hashed))
      owner
    } else {
      owner.meta.createWithMutableField(owner, this, Full(hashed))
    }
  }
}
