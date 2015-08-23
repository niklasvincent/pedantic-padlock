package info.lindblad.pedanticpadlock.bootstrap

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.redis.RedisClient
import info.lindblad.pedanticpadlock.model.{RedisScanStateStorage, ScanStateStorage, InMemoryScanStateStorage, ScanState}
import info.lindblad.pedanticpadlock.services.{ScanService, EventLoop}
import info.lindblad.pedanticpadlock.util.Logging
import spray.can.Http

import scala.concurrent.duration._

object Main extends App with Logging {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val system = ActorSystem("pedantic-padlock")

  val service = system.actorOf(Props[HttpRequestActor])

  val scanStateStorage: ScanStateStorage = {
    Configuration.redis match {
      case Some(redisConfiguration) => {
        logger.info(s"Got Redis configuration: ${redisConfiguration.host}:${redisConfiguration.port}. Creating new Redis client.")
        val redisClient = new RedisClient(host = redisConfiguration.host, port = redisConfiguration.port, secret = redisConfiguration.secret)
        new RedisScanStateStorage(redisClient)
      }
      case _ => {
        logger.info(s"No Redis configuration found. Using in memory data store.")
        new InMemoryScanStateStorage(new ConcurrentHashMap[String, ScanState]())
      }
    }
  }

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

