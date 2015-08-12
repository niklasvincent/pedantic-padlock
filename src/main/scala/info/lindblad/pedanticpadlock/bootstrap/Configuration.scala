package info.lindblad.pedanticpadlock.bootstrap

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.FiniteDuration
import scala.util.Properties
import com.typesafe.config.ConfigFactory

object Configuration {
  lazy val values = ConfigFactory.load().getConfig("pedantic-padlock")

  def duration(path: String, unit: TimeUnit): FiniteDuration = {
    new FiniteDuration(values.getDuration(path, unit), unit)
  }

  val interface = "0.0.0.0"

  lazy val port = Properties.envOrElse("PORT", "1337").toInt

  lazy val scheduledInterval: FiniteDuration = duration("scheduled-interval", TimeUnit.SECONDS)
}