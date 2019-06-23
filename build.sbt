name := "cs441_spring2019_project"

//version := "0.1"

scalaVersion := "2.12.8"

lazy val commonSettings = Seq(
  organization := "Shanks.lamda",
  version := "0.1"
)
lazy val app = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "LamdaGrpc"
  ).
  enablePlugins(AssemblyPlugin)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

libraryDependencies += "com.typesafe" % "config" % "1.3.2"
//libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
//libraryDependencies += "ch.qos.logback" % "logback-core" % "1.2.3"
//libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
//libraryDependencies += "org.slf4j" % "slf4j-api" % "1.8.0-beta2"
//libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.8.0-beta2"
// https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12
//libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.8.0-beta4"
// https://mvnrepository.com/artifact/org.slf4j/slf4j-jdk14
//libraryDependencies += "org.slf4j" % "slf4j-jdk14" % "1.8.0-beta2" % Test
// https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
// https://mvnrepository.com/artifact/org.slf4j/slf4j-nop
libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.8.0-beta4" % Test


libraryDependencies += "org.cloudsimplus" % "cloudsim-plus" % "4.3.4"
