package info.lindblad.pedanticpadlock.services

import info.lindblad.pedanticpadlock.asset.{Colors, StatusBadge}
import info.lindblad.pedanticpadlock.model.{FinishedResult, ScanState}
import info.lindblad.pedanticpadlock.util.Logging

object StatusBadgeService extends Logging {

  // Possible values: A+, A-, A-F, T (no trust) and M (certificate name mismatch)
  lazy val badges = Map(
    "A" -> new StatusBadge("A", Colors.green),
    "A-" -> new StatusBadge("A-", Colors.green),
    "A+" -> new StatusBadge("A+", Colors.green),
    "B" -> new StatusBadge("B", Colors.amber),
    "C" -> new StatusBadge("C", Colors.amber),
    "D" -> new StatusBadge("C", Colors.amber),
    "E" -> new StatusBadge("E", Colors.amber),
    "F" -> new StatusBadge("F", Colors.red),
    "T" -> new StatusBadge("T", Colors.red),
    "M" -> new StatusBadge("M", Colors.red)
  )

  def generateBadgeFromScanResult(scanState: Option[ScanState]): Option[StatusBadge] = {
    scanState match {
      case Some(scanState) => {
        scanState match {
          case scanResult: FinishedResult => Some(badges.get(scanResult.scanReport.grade.getOrElse("F")).getOrElse(default))
        }
      }
      case _ => None
    }
  }

  lazy val default = new StatusBadge("↺", Colors.grey)

}
