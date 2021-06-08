import sbt.librarymanagement.syntax._

object Dependencies {
  object V {
    val zio = "1.0.9"
    val zioCats = "2.5.1.0"
    val zioProcess = "0.4.0"
    val zioLogging = "0.5.10"
    val logback = "1.2.3"
    val circe = "0.14.1"
    val circeMagnolia = "0.7.0"
    val pureconfig = "0.15.0"
    val tapir = "0.18.0-M15"
    val derevo = "0.12.5"
  }

  val distributionDependencies = Seq(
    "dev.zio" %% "zio" % V.zio,
    "dev.zio" %% "zio-interop-cats" % V.zioCats,
    "dev.zio" %% "zio-macros" % V.zio,
    "dev.zio" %% "zio-process" % V.zioProcess,
    "dev.zio" %% "zio-logging-slf4j" % V.zioLogging,
    "ch.qos.logback" % "logback-classic" % V.logback,
    "io.circe" %% "circe-core" % V.circe,
    "io.circe" %% "circe-magnolia-derivation" % V.circeMagnolia,
    "io.circe" %% "circe-parser" % V.circe,
    "com.github.pureconfig" %% "pureconfig" % V.pureconfig,
    "com.github.pureconfig" %% "pureconfig-magnolia" % V.pureconfig,
    "com.softwaremill.sttp.tapir" %% "tapir-zio" % V.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-zio-http4s-server" % V.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % V.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-derevo" % V.tapir,
    "tf.tofu" %% "derevo-circe" % V.derevo
  )

  val testDependencies = Seq(
    "dev.zio" %% "zio-test" % V.zio
  )
}
