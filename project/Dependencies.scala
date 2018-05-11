import sbt._

object Dependencies {
  // Versions
  lazy val logbackVersion = "1.2.3"
  lazy val scalaLoggingVersion = "3.9.0"
  lazy val akkaVersion = "2.5.12"

  // Libraries
  val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVersion
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion
}