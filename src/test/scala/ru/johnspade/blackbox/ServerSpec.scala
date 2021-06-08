package ru.johnspade.blackbox

import org.http4s.{Method, Request, Uri}
import ru.johnspade.blackbox.Environments.AppEnvironment
import ru.johnspade.blackbox.http.Server
import zio.clock.Clock
import zio.interop.catz._
import zio.test.Assertion.equalTo
import zio.test._
import zio.test.environment.TestEnvironment
import zio.{Ref, ZIO}

import java.time.Instant
import scala.collection.immutable.SortedMap

object ServerSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[TestEnvironment, Throwable] = suite("StatsFlowSpec")(
    testM("should reduce stats") {
      for {
        statsRef <- Ref.make(Map(
          "baz" -> SortedMap(
            Instant.ofEpochSecond(1L) -> Map("amet" -> 1),
            Instant.ofEpochSecond(2L) -> Map("ipsum" -> 1)
          ),
          "foo" -> SortedMap(Instant.ofEpochSecond(2L) -> Map("lorem" -> 2))
        ))
        testEnv = Environments.appEnvironment.update[Ref[Stats]](_ => statsRef)
        responseOpt <- Server.routes.run(
          Request[ZIO[AppEnvironment with Clock, Throwable, *]](method = Method.GET, uri = Uri.unsafeFromString("/stats"))
        )
          .value
          .provideCustomLayer(testEnv)
        response <- responseOpt.get.as[String].provideCustomLayer(testEnv)
      } yield assert(response)(equalTo(
        """{"stats":[{"eventType":"baz","wordsCount":{"ipsum":1,"amet":1}},{"eventType":"foo","wordsCount":{"lorem":2}}]}"""
      ))
    },

    testM("should return an empty list for empty stats") {
      for {
        statsRef <- Ref.make(Stats.Empty)
        testEnv = Environments.appEnvironment.update[Ref[Stats]](_ => statsRef)
        responseOpt <- Server.routes.run(
          Request[ZIO[AppEnvironment with Clock, Throwable, *]](method = Method.GET, uri = Uri.unsafeFromString("/stats"))
        )
          .value
          .provideCustomLayer(testEnv)
        response <- responseOpt.get.as[String].provideCustomLayer(testEnv)
      } yield assert(response)(equalTo("""{"stats":[]}"""))
    }
  )
}
