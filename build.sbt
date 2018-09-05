import sbt._
import Keys._


// Variables:
val scioVersion = "0.5.5"
val scioLibVersion = "0.0.38"
val scioCommonVersion = scioVersion + "-lib" + scioLibVersion
val spotifyCoreDataSchemasVersion = "0.5.2"
val avroVersion = "1.8.2"
val protobufVersion = "3.3.1"
val ratatoolInternalVersion = "0.3.1"
val spotifyWebApiVersion = "2.0.4"

lazy val commonSettings = Defaults.coreDefaultSettings ++ Seq(
  organization          := "com.spotify.data.example",
  // Use semantic versioning http://semver.org/
  version               := "0.1.0-SNAPSHOT",
  scalaVersion          := "2.11.12",
  scalacOptions         ++= Seq(
    "-target:jvm-1.8",
    "-deprecation",
    "-feature",
    "-unchecked"),
  javacOptions          ++= Seq("-source", "1.8", "-target", "1.8"),
  packJarNameConvention := "original",

  // Repositories and dependencies
  resolvers ++= Seq(
    "Concurrent Maven Repo" at "http://conjars.org/repo",
    "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/",
    "Artifactory" at "https://artifactory.spotify.net/artifactory/repo",
    Resolver.mavenLocal
  ),

  // protobuf-lite is an older subset of protobuf-java and causes issues
  excludeDependencies += "com.google.protobuf" % "protobuf-lite",

  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val root: Project = Project(
  "spotify-codepub-parent",
  file(".")
).settings(
  commonSettings
).aggregate(
  schemas, pipeline
).enablePlugins(
  PackPlugin
)

lazy val pipeline: Project = Project(
  "spotify-codepub",
  file("spotify-codepub")
).settings(
  commonSettings,
  description := "spotify-codepub scio pipeline",
  libraryDependencies ++= Seq(
    "se.michaelthelin.spotify" % "spotify-web-api-java" % spotifyWebApiVersion,
    "com.spotify" %% "scio-core" % scioVersion,
    "com.spotify" %% "scio-test" % scioVersion % Test,
    "org.apache.beam" % "beam-runners-direct-java" % "2.4.0"
  ),
  // Modify the test output
  // (see "Specifying ScalaTest Arguments"
  //  at http://www.scalatest.org/user_guide/using_scalatest_with_sbt)
  // D: show all durations
  // F: show full stack traces
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF"),
  // Required for typed BigQuery macros:
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  packageOptions in (Compile, packageBin) +=
    Package.ManifestAttributes( "Class-Path" ->
      (managedClasspath in Runtime).value.files
        .map(f => f.getName)
        .filter(_.endsWith(".jar"))
        .mkString(" ")
    ),
  wartremoverErrors in Compile ++= Warts.unsafe.filterNot(disableWarts.contains),
  fork in run := true
).dependsOn(
  schemas
)

lazy val repl: Project = Project(
  "repl",
  file(".repl")
).settings(
  commonSettings,
  description := "Scio REPL for spotify-codepub. To start: `sbt repl/run`.",
  libraryDependencies ++= Seq(
    "com.spotify" %% "scio-repl" % scioVersion
  ),
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  mainClass in Compile := Some("com.spotify.scio.repl.ScioShell")
).dependsOn(
  pipeline
)

lazy val schemas: Project = Project(
  "spotify-codepub-schemas",
  file("spotify-codepub-schemas")
).settings(
  commonSettings,
  // This is a java only library so no scala version necessary
  crossPaths := false,
  autoScalaLibrary := false,
  javacOptions := Seq("-source", "1.7", "-target", "1.7"),
  version in AvroConfig := avroVersion,
  version in ProtobufConfig := protobufVersion,
  protobufRunProtoc in ProtobufConfig := (args =>
    // protoc-jar does not include 3.3.1 binary
    com.github.os72.protocjar.Protoc.runProtoc("-v3.3.0" +: args.toArray)
    ),
  // Avro and Protobuf files are compiled to src_managed/main/compiled_{avro,protobuf}
  // Exclude their parent to avoid confusing IntelliJ
  sourceDirectories in Compile := (sourceDirectories in Compile).value
    .filterNot(_.getPath.endsWith("/src_managed/main")),
  managedSourceDirectories in Compile := (managedSourceDirectories in Compile).value
    .filterNot(_.getPath.endsWith("/src_managed/main")),
  sources in doc in Compile := List(),  // suppress warnings
  compileOrder := CompileOrder.JavaThenScala
).enablePlugins(ProtobufPlugin)

// Run Scala style check as part of (before) tests:
lazy val testScalastyle = taskKey[Unit]("testScalastyle")
testScalastyle := org.scalastyle.sbt.ScalastylePlugin.autoImport.scalastyle.in(Test)
  .toTask("").value
(test in Test) := ((test in Test) dependsOn testScalastyle).value

// Disable some warts
val disableWarts = Set(
  Wart.Null,
  Wart.NonUnitStatements,
  Wart.Throw,
  // due to BQ macro:
  Wart.DefaultArguments,
  Wart.OptionPartial,
  Wart.Any)

addCommandAlias("verify", "; scalastyle; coverage; test; coverageReport; coverageOff")
