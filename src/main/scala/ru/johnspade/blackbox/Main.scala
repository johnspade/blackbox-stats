package ru.johnspade.blackbox

import ru.johnspade.blackbox.http.Server
import zio._
import zio.process.Command

object Main extends App {
  private val program =
    for {
      path <- ZIO.access[Has[AppConfig]](_.get.blackboxPath)
      _ <- StatsFlow.runFlow(Command(path).linesStream).fork
      server <- Server.runServer
    } yield server

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    program
      .provideSomeLayer(Environments.appEnvironment)
      .exitCode
}
