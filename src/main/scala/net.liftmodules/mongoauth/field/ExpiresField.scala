package net.liftmodules.mongoauth
package field

import java.util.Date

import org.joda.time.{ReadablePeriod, DateTime}

import net.liftweb._
import common._
import mongodb.record.BsonRecord
import mongodb.record.field.DateField

class ExpiresField[OwnerType <: BsonRecord[OwnerType]](rec: OwnerType) extends DateField(rec) {

  def this(rec: OwnerType, period: ReadablePeriod) = {
    this(rec)
    set(periodToExpiresDate(period))
  }

  def periodToExpiresDate(period: ReadablePeriod): Date = ((new DateTime).plus(period.toPeriod)).toDate

  def apply(in: ReadablePeriod): OwnerType = apply(Full(periodToExpiresDate(in)))

  def isExpired: Boolean = (new DateTime).getMillis >= (new DateTime(value)).getMillis
}
