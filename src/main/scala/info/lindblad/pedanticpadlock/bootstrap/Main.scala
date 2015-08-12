package info.lindblad.pedanticpadlock.bootstrap

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import info.lindblad.pedanticpadlock.model.{InMemoryScanStateStorage, ScanState}
import info.lindblad.pedanticpadlock.services.{ScanService, EventLoop}
import spray.can.Http

import scala.concurrent.duration._

object Main extends App {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val system = ActorSystem("pedantic-padlock")

  val service = system.actorOf(Props[HttpRequestActor])

  val scanStateStorage = new InMemoryScanStateStorage(new ConcurrentHashMap[String, ScanState]())
  system.scheduler.schedule(
    Configuration.scheduledInterval,
    Configuration.scheduledInterval)(
      EventLoop.tick(
        scanStateStorage,
        ScanService,
        System.currentTimeMillis(),
        Configuration.values.getLong("scan.expiry")
      )
    )

  implicit val timeout = Timeout(5.seconds)

  IO(Http) ? Http.Bind(service, interface = Configuration.interface, port = Configuration.port)
}

