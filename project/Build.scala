import sbt._
import sbt.Keys._

import com.typesafe.sbt.GitVersioning

object LiftModuleBuild extends Build with BuildSettings {

  val project = Project("lift-mongoauth", file("."))
    .settings(basicSettings:_*)
    .settings(publishSettings:_*)
    .settings(libraryDependencies <++= (liftVersion, scalaVersion) { (liftVersion, scalaVersion) =>
      val scalaTestVer = scalaVersion match {
        case v if (v.startsWith("2.10") || v.startsWith("2.11")) => "2.2.1"
        case _ => "1.9.2"
      }

      Seq(
        "net.liftweb" %% "lift-mongodb-record" % liftVersion % "provided",
        "net.liftweb" %% "lift-webkit" % liftVersion % "provided",
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
