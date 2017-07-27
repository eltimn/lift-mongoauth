import LiftModule.{liftVersion, liftEdition}

name := "mongoauth"
organization := "net.liftmodules"
liftVersion := "3.1.0"
liftEdition := liftVersion.value.substring(0,3)
moduleName := name.value + "_" + liftEdition.value

scalaVersion := "2.12.2"
crossScalaVersions := Seq("2.12.2", "2.11.11")
scalacOptions ++= Seq("-unchecked", "-deprecation")

libraryDependencies ++=
  "net.liftweb" %% "lift-mongodb-record" % liftVersion.value % "provided" ::
  "net.liftweb" %% "lift-webkit" % liftVersion.value % "provided" ::
  "org.scalatest" %% "scalatest" % "3.0.1" % "test" ::
  "org.mindrot" % "jbcrypt" % "0.3m" % "compile" ::
  Nil

enablePlugins(GitVersioning)

bintrayReleaseOnPublish in ThisBuild := false

LiftModule.bintrayPublishSettings
