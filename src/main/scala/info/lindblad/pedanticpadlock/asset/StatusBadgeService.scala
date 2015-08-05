package info.lindblad.pedanticpadlock.asset

import info.lindblad.pedanticpadlock.{QualysService, ConnectionTesterService}
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

  def generateBadge(hostname: String): Option[StatusBadge] = {
    logger.debug("Started analysing {}", hostname)
    // 1. Test initial connection
    val daysUntilExpiration = ConnectionTesterService.daysUntilExpiration(hostname)
    logger.debug("daysUntilExpiraton = {}", daysUntilExpiration)
    if (daysUntilExpiration == -1) {
      return Some(new StatusBadge("F", Colors.red))
    } else if (daysUntilExpiration <= 30) {
      return Some(new StatusBadge(daysUntilExpiration.toString, Colors.red))
    }

    // 2. Use Qualys API
    QualysService().gradeIfReady(hostname) match {
      case Some(grade) => Some(badges.get(grade).getOrElse(new StatusBadge("X", Colors.red)))
      case _ => None
    }
  }

  lazy val default = new StatusBadge("↺", Colors.grey)

}
