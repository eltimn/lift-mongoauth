import sbt._
import sbt.Keys._

object LiftModule {

  val liftVersion = settingKey[String]("Lift Web Framework full version number")

  val liftEdition = settingKey[String]("Lift Edition (such as 2.6 or 3.0)")

  val bintrayPublishSettings = Seq(
    pomExtra := {
      <scm>
        <url>git@github.com:eltimn/lift-mongoauth.git</url>
        <connection>scm:git:git@github.com:eltimn/lift-mongoauth.git</connection>
      </scm>
      <developers>
        <developer>
          <id>eltimn</id>
          <name>Tim Nelson</name>
          <url>http://eltimn.com/</url>
        </developer>
      </developers>
    },
    publishArtifact in Test := false,
    homepage := Some(url("https://github.com/eltimn/lift-mongoauth")),
    licenses := Seq(("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.txt"))),
    publishTo := Some("eltimn-maven" at "https://api.bintray.com/maven/eltimn/maven/lift-mongoauth/;publish=1")
  )

  val sonatypePublishSettings = Seq(
    publishTo := (version.value.endsWith("SNAPSHOT") match {
      case true  => Some("snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
      case false => Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
    }),

    credentials ++= (for {
      username <- Option(System.getenv().get("SONATYPE_USERNAME"))
      password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
    } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq,

    publishMavenStyle := true,

    publishArtifact in Test := false,

    pomIncludeRepository := { _ => false },

    pomExtra := (
      <url>https://github.com/liftmodules/amqp</url>
      <licenses>
        <license>
          <name>Apache 2.0 License</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:eltimn/lift-mongoauth.git</url>
        <connection>scm:git:git@github.com:eltimn/lift-mongoauth.git</connection>
      </scm>
      <developers>
        <developer>
          <id>eltimn</id>
          <name>Tim Nelson</name>
          <url>http://eltimn.com/</url>
        </developer>
      </developers>
    )
  )
}
