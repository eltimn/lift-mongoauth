name := "mongoauth"

liftVersion <<= liftVersion ?? "2.5-M4"

version <<= liftVersion apply { _ + "-0.3" }

organization := "net.liftmodules"

scalaVersion := "2.10.0"

crossScalaVersions := Seq("2.10.0", "2.9.2", "2.9.1-1", "2.9.1")

resolvers += "CB Central Mirror" at "http://repo.cloudbees.com/content/groups/public"

resolvers += "Sonatype OSS Release" at "http://oss.sonatype.org/content/repositories/releases"

resolvers += "Sonatype Snapshot" at "http://oss.sonatype.org/content/repositories/snapshots" 

libraryDependencies <++= (liftVersion) { liftVersion =>
  Seq(
    "net.liftweb" %% "lift-mongodb-record" % liftVersion % "compile",
    "ch.qos.logback" % "logback-classic" % "1.0.3" % "provided",
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

libraryDependencies <++= liftVersion { v =>
  "net.liftweb" %% "lift-webkit" % v % "compile->default" ::
  "net.liftweb" %% "lift-mapper" % v % "compile->default" ::
  Nil
}    

libraryDependencies ++= Seq(
  "org.scala-tools.testing" % "specs_2.9.0" % "1.6.8" % "test", // For specs.org tests
  "junit" % "junit" % "4.8" % "test->default", // For JUnit 4 testing
  "javax.servlet" % "servlet-api" % "2.5" % "provided->default",
  "com.h2database" % "h2" % "1.2.138", // In-process database, useful for development systems
  "ch.qos.logback" % "logback-classic" % "0.9.26" % "compile->default" // Logging
)


// Sonatype publishing set up below this point

publishTo <<= version { _.endsWith("SNAPSHOT") match {
 	case true  => Some("snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
 	case false => Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
  }
 } 

credentials += Credentials( file("sonatype.credentials") )

credentials += Credentials( file("/private/liftmodules/sonatype.credentials") )

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

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
  
