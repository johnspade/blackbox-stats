package ru.johnspade.blackbox

import cats.syntax.semigroup._
import io.circe.parser.decode
import zio.blocking.Blocking
import zio.clock.Clock
import zio.logging.{Logger, Logging}
import zio.macros.accessible
import zio.process.CommandError
import zio.stream.ZStream
import zio.{Has, Ref, URIO, URLayer, ZIO, ZLayer}

import scala.collection.immutable.SortedMap

@accessible
object StatsFlow {
  type StatsFlow = Has[Service]

  trait Service {
    def runFlow(stream: ZStream[Blocking, CommandError, String]): URIO[Blocking with Clock, Unit]
  }

  val live: URLayer[Has[AppConfig] with Has[Ref[Stats]] with Logging, StatsFlow] = ZLayer.fromServices[
    AppConfig,
    Ref[Stats],
    Logger[String],
    Service
  ](new LiveStatsFlow(_, _, _))
}

class LiveStatsFlow(
  appConfig: AppConfig,
  statsRef: Ref[Stats],
  logger: Logger[String],
) extends StatsFlow.Service {
  override def runFlow(stream: ZStream[Blocking, CommandError, String]): URIO[Blocking with Clock, Unit] =
    stream
      .mapM { line =>
        ZIO.fromEither(decode[Event](line))
          .tapError { e =>
            logger.debug(s"cannot parse '$line': ${e.getMessage}")
          }
          .either
      }
      .collectRight
      .tap { event =>
        val eventMap = SortedMap(event.timestamp -> Map(event.data -> 1))
        logger.info(event.toString) *>
          statsRef.update { stats =>
            stats.updatedWith(event.eventType) { statsByEvent =>
              val updated = statsByEvent.fold(eventMap)(_ combine eventMap)
              Some(updated.rangeFrom(updated.lastKey.minusMillis(appConfig.windowSize.toMillis)))
            }
          }
      }
      .runDrain
      .orDie
}
