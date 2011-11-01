package com.eltimn.auth.mongo

import org.joda.time.{ReadablePeriod, DateTime}

import net.liftweb._
import mongodb.record.BsonRecord
import mongodb.record.field.DateField

class ExpiresField[OwnerType <: BsonRecord[OwnerType]](rec: OwnerType) extends DateField(rec) {

  def this(rec: OwnerType, period: ReadablePeriod) = {
    this(rec)
    set(((new DateTime).plus(period.toPeriod)).toDate)
  }

  def isExpired: Boolean = (new DateTime).getMillis >= (new DateTime(value)).getMillis
}