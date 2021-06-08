package ru.johnspade.blackbox

import zio._

object Main extends App {
  private val program = StatsFlow.startFlow.fork *> Server.runServer

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    program
      .provideSomeLayer(Environments.appEnvironment)
      .exitCode
}
