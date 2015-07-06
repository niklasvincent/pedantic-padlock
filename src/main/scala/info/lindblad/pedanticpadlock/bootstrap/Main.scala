package info.lindblad.pedanticpadlock.bootstrap

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http

import scala.concurrent.duration._

import Configuration._

object Main extends App {
  implicit val system = ActorSystem("pedantic-padlock")

  val service = system.actorOf(Props[HttpRequestActor])

  implicit val timeout = Timeout(150.seconds)

  IO(Http) ? Http.Bind(service, interface = Configuration.interface, port = Configuration.port)
}

