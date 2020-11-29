package net.liftmodules.mongoauth
package field

import net.liftmodules.mongoauth.codecs.PermissionCodec
import net.liftweb.mongodb.record.field.MongoListField
import net.liftweb.mongodb.record.BsonRecord

import org.bson._
import org.bson.codecs.{BsonTypeCodecMap, DecoderContext, EncoderContext}
import org.bson.codecs.configuration.CodecRegistry

class PermissionListField[OwnerType <: BsonRecord[OwnerType]](rec: OwnerType)
  extends MongoListField[OwnerType, Permission](rec)
{
  private val codec = PermissionCodec()

  override protected def readValue(reader: BsonReader, decoderContext: DecoderContext, codecRegistry: CodecRegistry, bsonTypeCodecMap: BsonTypeCodecMap): Any = {
    reader.getCurrentBsonType match {
      case BsonType.STRING =>
        codec.decode(reader, decoderContext)
      case _ =>
        super.readValue(reader, decoderContext, codecRegistry, bsonTypeCodecMap)
    }
  }

  override protected def writeValue[T](writer: BsonWriter, encoderContext: EncoderContext, value: T, codecRegistry: CodecRegistry): Unit = {
    value match {
      case p: Permission =>
        writer.writeString(p.toString)
      case _ =>
        super.writeValue(writer, encoderContext, value, codecRegistry)
    }
  }
}
