package ru.johnspade.blackbox

import pureconfig.module.magnolia.semiauto.reader._
import pureconfig.{ConfigReader, ConfigSource}
import zio.{Has, ULayer, ZIO}

import scala.concurrent.duration.FiniteDuration

case class AppConfig(blackboxPath: String, windowSize: FiniteDuration)

object AppConfig {
  implicit val appConfigReader: ConfigReader[AppConfig] = deriveReader[AppConfig]

  val live: ULayer[Has[AppConfig]] = ZIO.effect {
    ConfigSource.default.at("app").loadOrThrow[AppConfig]
  }
    .toLayer
    .orDie
}
