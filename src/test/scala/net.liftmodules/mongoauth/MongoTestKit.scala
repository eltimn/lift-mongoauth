package net.liftmodules.mongoauth


import net.liftweb._
import mongodb._

import com.mongodb.{Mongo, ServerAddress}
import util.Props
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, WordSpec}


/**
 * Creates a Mongo instance named after the class.
 * Therefore, each Spec class shares the same database.
 * Database is dropped after.
 */
trait MongoTestKit extends BeforeAndAfter {
  this: WordSpec =>

  def dbName = "test_"+this.getClass.getName
    .replace(".", "_")
    .toLowerCase

  def defaultServer = new ServerAddress("127.0.0.1", 12345)

  // If you need more than one db, override this
  def dbs: List[(MongoIdentifier, ServerAddress, String)] = List((DefaultMongoIdentifier, defaultServer, dbName))

  def debug = false

  before {
    // define the dbs
    dbs foreach { case (id, srvr, name) =>
      MongoDB.defineDb(id, new Mongo(srvr), name)
    }
  }

    after {
    if (!debug) {
      // drop the databases
      dbs foreach { case (id, _, _) =>
        MongoDB.use(id) { db => db.dropDatabase }
      }
    }

    // clear the mongo instances
    MongoDB.close
  }
}

