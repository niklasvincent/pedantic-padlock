package info.lindblad.pedanticpadlock.services

import info.lindblad.pedanticpadlock.bootstrap.Configuration
import info.lindblad.pedanticpadlock.model._
import info.lindblad.pedanticpadlock.util.Logging

import scala.util.Try

abstract class ScanStateProcessing {

  def process(currentState: ScanState, currentTime: Long, validDuration: Long, startScan: NotProcessed => ScanState = startScan, pollScan: AwaitingResult => ScanState = pollScan): ScanState

  def startScan(notProcessed: NotProcessed): ScanState

  def pollScan(awaitingResult: AwaitingResult): ScanState

}

object ScanService extends ScanStateProcessing with Logging {

  def process(currentState: ScanState, currentTime: Long, validDuration: Long, startScan: NotProcessed => ScanState = startScan, pollScan: AwaitingResult => ScanState = pollScan): ScanState = {
    currentState match {
      case notProcessed: NotProcessed => startScan(notProcessed)
      case awaitingResult: AwaitingResult => pollScan(awaitingResult)
      case finishedResult: FinishedResult if ( finishedResult.isExpired(currentTime, validDuration) ) => startScan(new NotProcessed(finishedResult.domainName))
      case _ => currentState
    }
  }

    def startScan(notProcessed: NotProcessed): ScanState = {
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
          pollScan(new AwaitingResult(notProcessed.domainName, ScanReport(canConnect = true, daysUntilExpiration = Some(daysUntilExpiration))))
        }
        case _ => {
          notProcessed
        }
      }
    }

    def pollScan(awaitingResult: AwaitingResult): ScanState = {
      logger.info(s"Polling Qualsys report for ${awaitingResult.domainName}")
      logger.info(s"${awaitingResult.timesPolled} times polled previously")
      SslLabsService().analyse(awaitingResult.domainName) match {
        case None => new AwaitingResult(
          domainName = awaitingResult.domainName,
          scanReport = awaitingResult.scanReport,
          awaitingResult.timesPolled + 1
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
