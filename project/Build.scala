import sbt._
import sbt.Keys._

import com.typesafe.sbt.GitVersioning

object LiftModuleBuild extends Build with BuildSettings {

  val project = Project("lift-mongoauth", file("."))
    .settings(basicSettings:_*)
    .settings(publishSettings:_*)
    .settings(libraryDependencies ++= {
      val scalaTestVer = "2.2.1"

      Seq(
        "net.liftweb" %% "lift-mongodb-record" % liftVersion.value % "provided",
        "net.liftweb" %% "lift-webkit" % liftVersion.value % "provided",
        "ch.qos.logback" % "logback-classic" % "1.1.2" % "provided",
        "org.scalatest" %% "scalatest" % scalaTestVer % "test",
        "org.mindrot" % "jbcrypt" % "0.3m" % "compile"
      )
    })
    .settings(Seq(
      // Necessary beginning with sbt 0.13, otherwise Lift editions get messed up.
      // E.g. "2.5" gets converted to "2-5"
      moduleName := name.value
    ))
    .enablePlugins(GitVersioning)
}
