name := "mongoauth"

version := "2.4-0.3"

organization := "net.liftmodules"

scalaVersion := "2.9.2"

crossScalaVersions := Seq("2.9.2")

libraryDependencies <++= (scalaVersion) { scalaVersion =>
  val liftVersion = "2.5-RC1"
  Seq(
    "net.liftweb" %% "lift-mongodb-record" % liftVersion % "compile",
    "ch.qos.logback" % "logback-classic" % "1.0.6" % "provided",
    "org.scalatest" %% "scalatest" % "1.9.1" % "test",
    "org.mindrot" % "jbcrypt" % "0.3m" % "compile"
  )
}

scalacOptions <<= scalaVersion map { sv: String =>
  if (sv.startsWith("2.10."))
    Seq("-deprecation", "-unchecked", "-feature", "-language:postfixOps")
  else
    Seq("-deprecation", "-unchecked")
}

// To publish to the Cloudbees repos:

//publishTo := Some("liftmodules repository" at "https://repository-liftmodules.forge.cloudbees.com/release/")

//credentials += Credentials( file("/private/liftmodules/cloudbees.credentials") )
