package ru.johnspade.blackbox.http

import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.module.magnolia.semiauto.reader.deriveReader
import zio.{Has, ULayer, ZIO}

case class HttpConfig(host: String, port: Int)

object HttpConfig {
  implicit val httpConfigReader: ConfigReader[HttpConfig] = deriveReader[HttpConfig]

  val live: ULayer[Has[HttpConfig]] = ZIO.effect {
    ConfigSource.default.at("http").loadOrThrow[HttpConfig]
  }
    .toLayer
    .orDie
}
