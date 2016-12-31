import LiftModule.{liftVersion, liftEdition}

name := "mongoauth"

organization := "net.liftmodules"

liftVersion := "3.0.1"

liftEdition := liftVersion.value.substring(0,3)

moduleName := name.value + "_" + liftEdition.value

scalaVersion := "2.12.1"

crossScalaVersions := Seq("2.12.1", "2.11.8")

scalacOptions ++= Seq("-unchecked", "-deprecation")

resolvers += "Java.net Maven2 Repository" at "http://download.java.net/maven/2/"

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++=
  "net.liftweb" %% "lift-mongodb-record" % liftVersion.value % "provided" ::
  "net.liftweb" %% "lift-webkit" % liftVersion.value % "provided" ::
  // "ch.qos.logback" % "logback-classic" % "1.1.2" % "provided" ::
  "org.scalatest" %% "scalatest" % "3.0.1" % "test" ::
  "org.mindrot" % "jbcrypt" % "0.3m" % "compile" ::
  Nil

libraryDependencies += "com.rabbitmq" % "amqp-client" % "3.4.0"

LiftModule.bintrayPublishSettings

enablePlugins(GitVersioning, GitBranchPrompt)

git.baseVersion := "1.1"
