
import Dependencies._

lazy val commonSettings = Seq(
  organization := "com.bitbucket.tatianayavkina",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.6",
  fork in run := true
)

lazy val root  = (project in file("."))
  .settings(
    commonSettings
  )
  .settings(
    libraryDependencies ++= Seq(
      logbackClassic,
      scalaLogging,
      akkaStream
    )
  )