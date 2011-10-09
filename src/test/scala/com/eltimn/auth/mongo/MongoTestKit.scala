package com.eltimn
package auth.mongo

import org.specs.Specification

import net.liftweb._
import mongodb._

import com.mongodb.{Mongo, ServerAddress}

trait MongoTestKit {
  this: Specification =>

  def dbName = "test_"+this.getClass.getName
    .replace("$", "")
    .replace("net.liftweb.mongodb.", "")
    .replace(".", "_")
    .toLowerCase

  def defaultServer = new ServerAddress("127.0.0.1", 27017)

  // If you need more than one db, override this
  def dbs: List[(MongoIdentifier, ServerAddress, String)] = List((DefaultMongoIdentifier, defaultServer, dbName))

  def debug = false

  doBeforeSpec {
    // define the dbs
    dbs foreach { case (id, srvr, name) =>
      MongoDB.defineDb(id, new Mongo(srvr), name)
    }
  }

  def isMongoRunning: Boolean =
    try {
      if (dbs.length < 1)
        false
      else {
        dbs foreach { case (id, _, _) =>
          MongoDB.use(id) ( db => { db.getLastError } )
        }
        true
      }
    } catch {
      case e: Exception => false
    }

  def checkMongoIsRunning = isMongoRunning must beEqualTo(true).orSkipExample

  doAfterSpec {
    if (!debug && isMongoRunning) {
      // drop the databases
      dbs foreach { case (id, _, _) =>
        MongoDB.use(id) { db => db.dropDatabase }
      }
    }

    // clear the mongo instances
    MongoDB.close
  }
}

