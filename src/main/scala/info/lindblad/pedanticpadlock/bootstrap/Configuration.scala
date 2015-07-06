package info.lindblad.pedanticpadlock.bootstrap

import scala.util.Properties

object Configuration {

	val interface = "0.0.0.0"

	lazy val port = Properties.envOrElse("PORT", "8080").toInt

}