addSbtPlugin("com.cavorite" % "sbt-avro-1-8" % "1.1.3")
addSbtPlugin("com.github.gseitz" % "sbt-protobuf" % "0.6.3")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.9.3")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.2.0")
addSbtPlugin("org.xerial.sbt" % "sbt-pack" % "0.10.1")
addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.5.0")

libraryDependencies ++= Seq(
  "com.github.os72" % "protoc-jar" % "3.3.0.1"
)
