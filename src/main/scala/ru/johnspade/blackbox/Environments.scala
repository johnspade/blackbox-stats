package ru.johnspade.blackbox

import ru.johnspade.blackbox.StatsFlow.StatsFlow
import ru.johnspade.blackbox.http.HttpConfig
import zio.blocking.Blocking
import zio.clock.Clock
import zio.logging.Logging
import zio.logging.slf4j.Slf4jLogger
import zio.{Has, Ref, ULayer, URLayer, ZLayer}

object Environments {
  type AppEnvironment = StatsFlow with Has[Ref[Stats]] with Has[AppConfig] with Has[HttpConfig] with Blocking with Clock

  private val statsLayer: ZLayer[Any, Nothing, Has[Ref[Stats]]] = Ref.make(Stats.Empty).toLayer
  private val logger: ULayer[Logging] = Slf4jLogger.make((_, message) => message)

  val appEnvironment: URLayer[Blocking with Clock, AppEnvironment] =
    (logger ++ AppConfig.live >+> statsLayer >+> StatsFlow.live) ++
      HttpConfig.live ++ ZLayer.requires[Blocking with Clock]
}
