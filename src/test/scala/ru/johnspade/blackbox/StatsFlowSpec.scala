package ru.johnspade.blackbox

import zio.logging.Logging
import zio.logging.slf4j.Slf4jLogger
import zio.test.Assertion.equalTo
import zio.test.environment.TestEnvironment
import zio.test.{DefaultRunnableSpec, ZSpec, _}
import zio.{Has, Ref, ULayer, ZIO, ZLayer}

import java.time.Instant
import scala.collection.immutable.SortedMap

object StatsFlowSpec extends DefaultRunnableSpec {
  private val appConfig = AppConfig.live
  private val logger: ULayer[Logging] = Slf4jLogger.make((_, message) => message)

  private val flowLayer = appConfig ++ ZLayer.requires[Has[Ref[Stats]]] ++ logger >>> StatsFlow.live

  override def spec: ZSpec[TestEnvironment, Throwable] = suite("StatsFlowSpec")(
    testM("should count words in a single time window") {
      val statsRef = Ref.make(Stats.Empty)
      val stream = zio.stream.Stream(
        """{ "event_type": "baz", "data": "amet", "timestamp": 1 }""",
        """{ "event_type": "foo", "data": "lorem", "timestamp": 2 }""",
        """{ "event_type": "foo", "data": "lorem", "timestamp": 2 }""",
        """{ "event_type": "baz", "data": "ipsum", "timestamp": 2 }"""
      )
      (for {
        _ <- StatsFlow.runFlow(stream)
        assertion <- assertM(ZIO.accessM[Has[Ref[Stats]]](_.get.get))(equalTo(Map(
          "baz" -> SortedMap(
            Instant.ofEpochSecond(1L) -> Map("amet" -> 1),
            Instant.ofEpochSecond(2L) -> Map("ipsum" -> 1)
          ),
          "foo" -> SortedMap(Instant.ofEpochSecond(2L) -> Map("lorem" -> 2))
        )))
      } yield assertion)
        .provideCustomLayer(statsRef.toLayer >+> flowLayer)
    },

    testM("should not count words outside of a time window") {
      val statsRef = Ref.make(Stats.Empty)
      val stream = zio.stream.Stream(
        """{ "event_type": "baz", "data": "amet", "timestamp": 1 }""",
        """{ "event_type": "foo", "data": "lorem", "timestamp": 1000 }""",
        """{ "event_type": "foo", "data": "lorem", "timestamp": 1000 }""",
        """{ "event_type": "baz", "data": "ipsum", "timestamp": 1000 }"""
      )
      (for {
        _ <- StatsFlow.runFlow(stream)
        assertion <- assertM(ZIO.accessM[Has[Ref[Stats]]](_.get.get))(equalTo(Map(
          "baz" -> SortedMap(
            Instant.ofEpochSecond(1000L) -> Map("ipsum" -> 1)
          ),
          "foo" -> SortedMap(Instant.ofEpochSecond(1000L) -> Map("lorem" -> 2))
        )))
      } yield assertion)
        .provideCustomLayer(statsRef.toLayer >+> flowLayer)
    },

    testM("should skip garbage lines") {
      val statsRef = Ref.make(Stats.Empty)
      val stream = zio.stream.Stream(
        """{ "event_type": "baz", "data": "amet", "timestamp": 1 }""",
        """{ "p�����2�|""",
        """{ "event_type": "foo", "data": "lorem", "timestamp": 2 }""",
        """{ "event_type": "foo", "data": "lorem", "timestamp": 2 }""",
        """{ "H��vY�;�""",
        """{ "event_type": "baz", "data": "ipsum", "timestamp": 2 }"""
      )
      (for {
        _ <- StatsFlow.runFlow(stream)
        assertion <- assertM(ZIO.accessM[Has[Ref[Stats]]](_.get.get))(equalTo(Map(
          "baz" -> SortedMap(
            Instant.ofEpochSecond(1L) -> Map("amet" -> 1),
            Instant.ofEpochSecond(2L) -> Map("ipsum" -> 1)
          ),
          "foo" -> SortedMap(Instant.ofEpochSecond(2L) -> Map("lorem" -> 2))
        )))
      } yield assertion)
        .provideCustomLayer(statsRef.toLayer >+> flowLayer)
    }
  )
}
