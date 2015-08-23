package info.lindblad.pedanticpadlock.bootstrap

import java.net.URI
import java.util.concurrent.TimeUnit

import scala.concurrent.duration.FiniteDuration
import scala.util.{Try, Properties}
import com.typesafe.config.ConfigFactory

object Configuration {
  lazy val values = ConfigFactory.load().getConfig("pedantic-padlock")

  def duration(path: String, unit: TimeUnit): FiniteDuration = {
    new FiniteDuration(values.getDuration(path, unit), unit)
  }

  val interface = "0.0.0.0"

  lazy val port = Properties.envOrElse("PORT", "1337").toInt

  lazy val scheduledInterval: FiniteDuration = duration("scheduled-interval", TimeUnit.SECONDS)

  /**
   * Attempt to retrieve Redis connection details in the following order:
   *
   * 1) REDISCLOUD_URL environment variable (Heroku)
   * 2) pedantic-padlock.redis-url in application.conf
   *
   * @return Option[RedisConfiguration]
   */
  def redis: Option[RedisConfiguration] = {
    Properties.envOrSome("REDISCLOUD_URL", Try(values.getString("redis-url")).toOption) match {
      case Some(redisUrl) => {
        val redisUri = new URI(redisUrl)
        val host = redisUri.getHost()
        val port = redisUri.getPort()
        val secret = Try(redisUri.getUserInfo().split(":",2).last).toOption
        Some(RedisConfiguration(host, port, secret))
      }
      case _ => None
    }
  }
}

case class RedisConfiguration(host: String, port: Int, secret: Option[String])