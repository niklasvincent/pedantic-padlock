package info.lindblad.pedanticpadlock.model

/* This represents the current state of a scan */
trait ScanState {

}

class NotProcessed(val domainName: String, val timeCreated: Long = System.currentTimeMillis()) extends ScanState

class AwaitingResult(val domainName: String, val scanReport: ScanReport, val timesPolled: Int = 0, val timeCreated: Long = System.currentTimeMillis()) extends ScanState

class FinishedResult(val domainName: String, val scanReport: ScanReport, val timeCreated: Long = System.currentTimeMillis()) extends ScanState {

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