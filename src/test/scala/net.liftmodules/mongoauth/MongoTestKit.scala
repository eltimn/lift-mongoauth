package net.liftmodules.mongoauth

import java.util.regex.Pattern

import org.scalatest.BeforeAndAfter
import org.scalatest.wordspec.AnyWordSpec

import net.liftweb.mongodb._
import net.liftweb.mongodb.codecs._
import net.liftweb.mongodb.record.MongoRecordRules
import net.liftweb.util.{DefaultConnectionIdentifier, ConnectionIdentifier, Props}

import net.liftmodules.mongoauth.codecs.PermissionCodec

import org.bson.{BsonDocument, BsonType}
import org.bson.codecs.configuration.{CodecRegistry, CodecRegistries}
import com.mongodb.{MongoClient, MongoClientSettings, ServerAddress}

trait MongoTestKit extends BeforeAndAfter {
  this: AnyWordSpec =>

  // val bsonTypeClassMap: BsonTypeClassMap =
  //   BsonTypeClassMap(
  //     (BsonType.REGULAR_EXPRESSION -> classOf[Pattern]),
  //     (BsonType.BINARY -> classOf[Array[Byte]]),
  //     (BsonType.DOCUMENT, classOf[BsonDocument])
  //   )

  // val registry: CodecRegistry = CodecRegistries.fromRegistries(
  //   MongoClientSettings.getDefaultCodecRegistry(),
  //   CodecRegistries.fromCodecs(BigDecimalStringCodec(), CalendarCodec(), JodaDateTimeCodec(), PermissionCodec())
  // )

  // MongoRecordRules.defaultCodecRegistry.default.set(registry)
  // MongoRecordRules.defaultBsonTypeClassMap.default.set(bsonTypeClassMap)

  def dbName = "test_"+this.getClass.getName
    .replace(".", "_")
    .toLowerCase

  def mongo = new MongoClient("127.0.0.1", Props.getInt("mongo.default.port", 27017))

  // If you need more than one db, override this
  def dbs: List[(ConnectionIdentifier, String)] = List((DefaultConnectionIdentifier, dbName))

  def debug = false

  before {
    // define the dbs
    dbs foreach { case (id, db) =>
      MongoDB.defineDb(id, mongo, db)
    }
  }

  after {
    if (!debug) {
      // drop the databases
      dbs foreach { case (id, _) =>
        MongoDB.useDatabase(id) { db => db.drop() }
      }
    }

    // clear the mongo instances
    MongoDB.closeAll()
  }
}

