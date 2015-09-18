package info.lindblad.pedanticpadlock.services

import info.lindblad.pedanticpadlock.bootstrap.Configuration
import info.lindblad.pedanticpadlock.model._
import info.lindblad.pedanticpadlock.util.Logging

import scala.util.Try

abstract class ScanStateProcessing {

  def process(currentState: ScanState, currentTime: Long, validDuration: Long, startScan: (NotProcessed, Long) => ScanState = startScan, pollScan: (AwaitingResult, Long) => ScanState = pollScan): ScanState

  def startScan(notProcessed: NotProcessed, currentTime: Long): ScanState

  def pollScan(awaitingResult: AwaitingResult, currentTime: Long): ScanState

}

object ScanService extends ScanStateProcessing with Logging {

  def process(currentState: ScanState, currentTime: Long, validDuration: Long, startScan: (NotProcessed, Long) => ScanState = startScan, pollScan: (AwaitingResult, Long) => ScanState = pollScan): ScanState = {
    currentState match {
      case notProcessed: NotProcessed => startScan(notProcessed, currentTime)
      case awaitingResult: AwaitingResult => pollScan(awaitingResult, currentTime)
      case finishedResult: FinishedResult if ( finishedResult.isExpired(currentTime, validDuration) ) => startScan(new NotProcessed(finishedResult.domainName), currentTime)
      case _ => currentState
    }
  }

  def startScan(notProcessed: NotProcessed, currentTime: Long): ScanState = {
    val warningDaysBeforeExpiry = Try(Configuration.values.getInt("scan.warning-days-before-expiry")).getOrElse(30)
    ConnectionProbeService.probe(notProcessed.domainName) match {
      case ConnectionProbeResult(false, _) => {
        logger.info("Java cannot connect to {}", notProcessed.domainName)
        new FinishedResult(notProcessed.domainName, ScanReport(grade = Some("F"), reason = Some("Java cannot connect"), canConnect = false))
      }
      case ConnectionProbeResult(true, Some(daysUntilExpiration)) if (daysUntilExpiration <= warningDaysBeforeExpiry) => {
        logger.info("Certificate for {} is expiring soon", notProcessed.domainName)
        new FinishedResult(notProcessed.domainName, ScanReport(grade = Some(daysUntilExpiration.toString), reason = Some("Certificate about to expire soon"), canConnect = false))
      }
      case ConnectionProbeResult(true, Some(daysUntilExpiration)) if (daysUntilExpiration > warningDaysBeforeExpiry) => {
        logger.info("Could connect to {} and certificate expiration is more than {} days away", notProcessed.domainName, warningDaysBeforeExpiry)
        pollScan(new AwaitingResult(notProcessed.domainName, ScanReport(canConnect = true, daysUntilExpiration = Some(daysUntilExpiration))), currentTime)
      }
      case _ => {
        notProcessed
      }
    }
  }

  def pollScan(awaitingResult: AwaitingResult, currentTime: Long): ScanState = {
    SslLabsService().analyse(awaitingResult.domainName, currentTime, awaitingResult.lastTimePolled, Configuration.sslLabsPollInterval) match {
      case None => new AwaitingResult(
        domainName = awaitingResult.domainName,
        scanReport = awaitingResult.scanReport,
        timesPolled = awaitingResult.timesPolled + 1,
        lastTimePolled = currentTime
      )
      case Some(report) => new FinishedResult(
        awaitingResult.domainName,
        scanReport = ScanReport(
          canConnect = true,
          grade = Some(report.grade),
          reason = Some("Grade given by Qualys SSL Labs")
        )
      )
    }
  }

}
