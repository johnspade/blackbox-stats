import Dependencies._

ThisBuild / scalaVersion := "2.13.6"
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / organization := "ru.johnspade"

ThisBuild / scalacOptions ++= Seq(
  "-language:higherKinds",
  "-Ymacro-annotations"
)

name := "blackbox-stats"
libraryDependencies ++= distributionDependencies ++ testDependencies.map(_ % Test)
testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.0" cross CrossVersion.full)
