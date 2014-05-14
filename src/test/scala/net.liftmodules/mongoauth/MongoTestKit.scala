package net.liftmodules.mongoauth

import org.scalatest.{BeforeAndAfter, WordSpec}

import net.liftweb._
import mongodb._
import util.{DefaultConnectionIdentifier, ConnectionIdentifier}

import com.mongodb.{MongoClient, ServerAddress}

trait MongoTestKit extends BeforeAndAfter {
  this: WordSpec =>

  def dbName = "test_"+this.getClass.getName
    .replace(".", "_")
    .toLowerCase

  def mongo = new MongoClient("127.0.0.1", 27017)

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
        MongoDB.use(id) { db => db.dropDatabase() }
      }
    }

    // clear the mongo instances
    MongoDB.closeAll()
  }
}

