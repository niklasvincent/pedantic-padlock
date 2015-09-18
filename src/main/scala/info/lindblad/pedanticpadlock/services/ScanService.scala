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
    def probe(domainName: String): ScanReport = {
      ConnectionProbeService.probe(notProcessed.domainName) match {
        case connectionProbeResult: ConnectionProbeResult => ScanReport(canConnect = connectionProbeResult.canConnect, daysUntilExpiration = connectionProbeResult.daysUntilExpiration)
      }
    }

    val awaitingResult = new AwaitingResult(domainName = notProcessed.domainName, scanReport = probe(notProcessed.domainName))
    pollScan(awaitingResult, currentTime)
  }

  def pollScan(awaitingResult: AwaitingResult, currentTime: Long): ScanState = {
    SslLabsService().analyse(awaitingResult, currentTime, awaitingResult.lastTimePolled, Configuration.sslLabsPollInterval) match {
      case Right(sslLabsReport) => {
        new FinishedResult(
          awaitingResult.domainName,
          scanReport = ScanReport(
            canConnect = true,
            grade = Some(sslLabsReport.grade),
            reason = Some("Grade given by Qualys SSL Labs")
          )
        )
      }
      case Left(newAwaitingResult) => newAwaitingResult
    }
  }

}
