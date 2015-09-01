
lazy val root = 
  (project in file("."))
    .settings(scalaVersion := "2.11.7")
    .aggregate(`quill-core`, `quill-sql`, `quill-jdbc`, `quill-finagle-mysql`)

lazy val `quill-core` = 
  (project in file("quill-core"))
    .settings(commonSettings: _*)
    .settings(libraryDependencies ++= Seq(
      "com.typesafe"               %  "config"        % "1.3.0",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
      "org.scala-lang"             %  "scala-reflect" % scalaVersion.value
    ))

lazy val `quill-sql` = 
  (project in file("quill-sql"))
    .settings(commonSettings: _*)
    .dependsOn(`quill-core` % "compile->compile;test->test")

lazy val `quill-jdbc` = 
  (project in file("quill-jdbc"))
    .settings(commonSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        "com.zaxxer" % "HikariCP"             % "2.3.9",
        "mysql"      % "mysql-connector-java" % "5.1.36" % "test"
      ),
      parallelExecution in Test := false
    )
    .dependsOn(`quill-sql` % "compile->compile;test->test")

lazy val `quill-finagle-mysql` = 
  (project in file("quill-fingle-mysql"))
    .settings(commonSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        "com.twitter" %% "finagle-mysql" % "6.27.0"
      ),
      parallelExecution in Test := false
    )
    .dependsOn(`quill-sql` % "compile->compile;test->test")

lazy val commonSettings = releaseSettings ++ Seq(
  organization := "io.getquill",
  scalaVersion := "2.11.7",
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest"       % "2.2.4" % "test",
    "ch.qos.logback" % "logback-classic" % "1.1.3" % "test"
  ),
  scalacOptions ++= Seq("-deprecation", "-feature", "-Xfatal-warnings", "-optimize", "-unchecked"),
  EclipseKeys.eclipseOutput := Some("bin"),
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  pomExtra := (
    <url>http://github.com/fwbrasil/quill</url>
    <licenses>
      <license>
        <name>LGPL</name>
        <url>https://raw.githubusercontent.com/fwbrasil/quill/master/LICENSE-LGPL.txt</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:fwbrasil/quill.git</url>
      <connection>scm:git:git@github.com:fwbrasil/quill.git</connection>
    </scm>
    <developers>
      <developer>
        <id>fwbrasil</id>
        <name>Flavio W. Brasil</name>
        <url>http://github.com/fwbrasil/</url>
      </developer>
    </developers>)
)

