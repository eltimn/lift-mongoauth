import sbt._
import sbt.Keys._

object BuildSettings {

  val resolutionRepos = Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )

  val liftVersion = SettingKey[String]("liftVersion", "Version number of the Lift Web Framework")
  val liftEdition = SettingKey[String]("liftEdition", "Lift Edition (short version number to append to artifact name)")

  val basicSettings = Defaults.defaultSettings ++ Seq(
    name := "mongoauth",
    organization := "net.liftmodules",
    version := "0.6",
    scalaVersion := "2.11.5",
    liftVersion <<= liftVersion ?? "2.6",
    liftEdition <<= liftVersion apply { _.substring(0,3) },
    name <<= (name, liftEdition) { (n, e) =>  n + "_" + e },
    crossScalaVersions <<= liftEdition { le => le match {
      case "3.0" => Seq("2.11.5")
      case _ => Seq("2.9.2", "2.10.4", "2.11.5")
    }},
    scalacOptions <<= scalaBinaryVersion map { sbv => sbv match {
      case "2.9.2" => Seq("-deprecation", "-unchecked")
      case _ => Seq("-deprecation", "-unchecked", "-feature", "-language:postfixOps", "-language:implicitConversions")
    }},
    resolvers ++= resolutionRepos
  )

  val publishSettings = seq(
    resolvers += "CB Central Mirror" at "http://repo.cloudbees.com/content/groups/public",

    publishTo <<= version { _.endsWith("SNAPSHOT") match {
      case true  => Some("snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
      case false => Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
    }},

    credentials += Credentials( file("sonatype.credentials") ),
    credentials += Credentials( file("/private/liftmodules/sonatype.credentials") ),
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    pomExtra := (
      <url>https://github.com/eltimn/lift-mongoauth</url>
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
