import Dependencies._

ThisBuild / scalaVersion := "2.13.6"
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / organization := "ru.johnspade"

ThisBuild / scalacOptions ++= Seq(
  "-language:higherKinds",
  "-Ymacro-annotations"
)

lazy val root = (project in file("."))
  .settings(
    name := "blackbox-stats",
    libraryDependencies ++= distributionDependencies ++ testDependencies.map(_ % Test),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
