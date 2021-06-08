package ru.johnspade.blackbox

import cats.syntax.semigroup._
import derevo.circe.{decoder, encoder}
import derevo.derive
import sttp.tapir.derevo.schema
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.ztapir._
import zio.{Has, Ref, ZIO}

import scala.collection.immutable.ListMap

object Endpoints {
  val statsEndpoint: ZServerEndpoint[Has[Ref[Stats]], Unit, Unit, StatsModel] =
    endpoint.get
      .in("stats")
      .out(jsonBody[StatsModel])
      .zServerLogic { _ =>
        ZIO.accessM[Has[Ref[Stats]]](_.get.get)
          .map { stats =>
            val reduced = stats.view.mapValues(_.values.reduce(_ combine _))
            val transformed = reduced.map {
              case (eventType, counts) => EventTypeStats(
                eventType,
                ListMap.from(counts.toVector.sortBy {
                  case (_, count) => count
                }(Ordering[Int].reverse))
              )
            }
              .toVector

            StatsModel(transformed)
          }
      }

  @derive(encoder, decoder, schema)
  case class StatsModel(stats: Vector[EventTypeStats])

  @derive(encoder, decoder, schema)
  case class EventTypeStats(eventType: String, wordsCount: Map[String, Int])
}
