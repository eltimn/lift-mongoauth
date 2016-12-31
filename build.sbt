import LiftModule.{liftVersion, liftEdition}

name := "mongoauth"

organization := "net.liftmodules"

liftVersion := "3.0.1"

liftEdition := liftVersion.value.substring(0,3)

moduleName := name.value + "_" + liftEdition.value

scalaVersion := "2.12.1"

crossScalaVersions := Seq("2.12.1", "2.11.8")

scalacOptions ++= Seq("-unchecked", "-deprecation")

libraryDependencies ++=
  "net.liftweb" %% "lift-mongodb-record" % liftVersion.value % "provided" ::
  "net.liftweb" %% "lift-webkit" % liftVersion.value % "provided" ::
  "org.scalatest" %% "scalatest" % "3.0.1" % "test" ::
  "org.mindrot" % "jbcrypt" % "0.3m" % "compile" ::
  Nil

LiftModule.bintrayPublishSettings

enablePlugins(GitVersioning)

git.baseVersion := "1.1"
