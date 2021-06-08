package ru.johnspade.blackbox

import cats.syntax.semigroup._
import io.circe.parser.decode
import ru.johnspade.blackbox.Configuration.AppConfig
import zio.blocking.Blocking
import zio.clock.Clock
import zio.logging.{Logger, Logging}
import zio.macros.accessible
import zio.process.Command
import zio.{Has, Ref, URIO, URLayer, ZIO, ZLayer}

import scala.collection.immutable.SortedMap

@accessible
object StatsFlow {
  type StatsFlow = Has[Service]

  trait Service {
    def startFlow: URIO[Blocking with Clock, Unit]
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
  private val command = Command(appConfig.blackboxPath)

  override val startFlow: URIO[Blocking with Clock, Unit] =
    command.linesStream
      .mapM { line =>
        ZIO.fromEither(decode[Event](line))
          .tapError { e =>
            logger.debug(s"can't parse line $line: ${e.getMessage}")
          }
          .either
      }
      .collectRight
      .tap(event => logger.info(event.toString))
      .mapM { event =>
        val eventMap = SortedMap(event.timestamp -> Map(event.data -> 1))
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
