name := "mongoauth"

version := "2.5-M1-0.3"

organization := "net.liftmodules"

scalaVersion := "2.9.1"

crossScalaVersions := Seq("2.9.2", "2.9.1-1", "2.9.1")

libraryDependencies <++= (scalaVersion) { scalaVersion =>
  val liftVersion = "2.5-M1"
  Seq(
    "net.liftweb" %% "lift-mongodb-record" % liftVersion % "compile",
    "ch.qos.logback" % "logback-classic" % "1.0.3" % "provided",
    "org.scalatest" %% "scalatest" % "1.8" % "test",
    "org.mindrot" % "jbcrypt" % "0.3m" % "compile"
  )
}

scalacOptions ++= Seq("-deprecation", "-unchecked")

// To publish to the Cloudbees repos:

publishTo := Some("liftmodules repository" at "https://repository-liftmodules.forge.cloudbees.com/release/")

credentials += Credentials( file("/private/liftmodules/cloudbees.credentials") )
