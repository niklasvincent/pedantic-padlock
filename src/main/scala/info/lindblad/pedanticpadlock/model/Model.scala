package info.lindblad.pedanticpadlock.model

/* This represents the current state of a scan */
abstract class ScanState(val domainName: String) {

  def currentTime() = System.currentTimeMillis()

  val timeCreated: Long = currentTime()

}

class NotProcessed(override val domainName: String) extends ScanState(domainName: String)

class AwaitingResult(override val domainName: String, val scanReport: ScanReport, val timesPolled: Int = 0) extends ScanState(domainName: String)

class FinishedResult(override val domainName: String, val scanReport: ScanReport) extends ScanState(domainName: String) {

  def isExpired(now: Long, validDuration: Long): Boolean = {
    val expirationTime = timeCreated + validDuration
    now >= expirationTime
  }

}

case class ScanReport(
  grade: Option[String] = None,
  reason: Option[String] = None,
  daysUntilExpiration: Option[Long] = None,
  canConnect: Boolean,
  sslLabsReport: Option[SslLabsReport] = None
)

case class SslLabsReport(
  grade: String,
  hasWarnings: Boolean,
  isExceptional: Boolean,
  forwardSecrecy: Boolean,
  keyStrength: Int,
  algorithm: String
)

case class ConnectionProbeResult(canConnect: Boolean, daysUntilExpiration: Option[Long])