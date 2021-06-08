package ru.johnspade.blackbox

import pureconfig.module.magnolia.semiauto.reader._
import pureconfig.{ConfigReader, ConfigSource}
import zio.{Has, ULayer, ZIO}

import scala.concurrent.duration.FiniteDuration

object Configuration {
  case class AppConfig(blackboxPath: String, windowSize: FiniteDuration)
  object AppConfig {
    implicit val appConfigReader: ConfigReader[AppConfig] = deriveReader[AppConfig]
  }

  val liveAppConfig: ULayer[Has[AppConfig]] = ZIO.effect {
    ConfigSource.default.at("app").loadOrThrow[AppConfig]
  }
    .toLayer
    .orDie

  case class HttpConfig(host: String, port: Int)
  object HttpConfig {
    implicit val httpConfigReader: ConfigReader[HttpConfig] = deriveReader[HttpConfig]
  }

  val liveHttpConfig: ULayer[Has[HttpConfig]] = ZIO.effect {
    ConfigSource.default.at("http").loadOrThrow[HttpConfig]
  }
    .toLayer
    .orDie
}
