name := "lift-auth-mongo"

version := "0.1-SNAPSHOT"

organization := "com.eltimn"

scalaVersion := "2.9.1"

crossScalaVersions := Seq("2.9.1", "2.9.0-1", "2.9.0", "2.8.2", "2.8.1", "2.8.0")

libraryDependencies <++= (scalaVersion) { scalaVersion =>
  val scalatestVersion = scalaVersion match {
    case "2.8.0" => "1.3"
    case "2.8.1" | "2.8.2" => "1.5.1"
    case _       => "1.6.1"
  }
  val specsVersion = scalaVersion match {
    case "2.8.0" => "1.6.5"
    case "2.9.1" => "1.6.9"
    case _       => "1.6.8"
  }
  val liftVersion = scalaVersion match {
    //case "2.9.1" => "2.4-M4"
    case _       => "2.4-SNAPSHOT"
  }
  Seq(
    "net.liftweb" %% "lift-mongodb-record" % liftVersion % "compile",
    "ch.qos.logback" % "logback-classic" % "0.9.26" % "provided",
    "org.scalatest" %% "scalatest" % scalatestVersion % "test",
    "org.mindrot" % "jbcrypt" % "0.3m" % "compile"
  )
}

scalacOptions ++= Seq("-deprecation", "-unchecked")

//defaultExcludes ~= (_ || "*~")
