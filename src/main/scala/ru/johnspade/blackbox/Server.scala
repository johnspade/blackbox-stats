package ru.johnspade.blackbox

import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import ru.johnspade.blackbox.Configuration.HttpConfig
import ru.johnspade.blackbox.Environments.AppEnvironment
import sttp.tapir.server.http4s.Http4sServerOptions
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.ztapir._
import zio.interop.catz._
import zio.{RIO, Task, ZIO}

object Server {
  private implicit val serverOptions: Http4sServerOptions[Task, Task] = Http4sServerOptions.default[Task, Task]

  private val routes = ZHttp4sServerInterpreter.from(Endpoints.statsEndpoint.widen[AppEnvironment]).toRoutes

  def runServer: RIO[AppEnvironment, Nothing] =
    for {
      implicit0(rts: zio.Runtime[AppEnvironment]) <- ZIO.runtime[AppEnvironment]
      cfg = rts.environment.get[HttpConfig]
      ec = rts.platform.executor.asEC
      startedServer <- BlazeServerBuilder(ec)
        .bindHttp(cfg.port, cfg.host)
        .withHttpApp(Router("/" -> routes).orNotFound)
        .resource
        .toManaged
        .useForever
    } yield startedServer
}
