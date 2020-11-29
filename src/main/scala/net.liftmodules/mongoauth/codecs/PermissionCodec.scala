package net.liftmodules.mongoauth
package codecs

import org.bson.{BsonReader, BsonWriter}
import org.bson.codecs._

/**
 * A Codec for Permission instances.
 */
case class PermissionCodec() extends Codec[Permission] {
  override def encode(writer: BsonWriter, value: Permission, encoderContext: EncoderContext): Unit = {
    writer.writeString(value.toString)
  }

  override def decode(reader: BsonReader, decoderContext: DecoderContext): Permission = {
    Permission.fromString(reader.readString())
  }

  override def getEncoderClass(): Class[Permission] = classOf[Permission]
}
