package info.lindblad.pedanticpadlock.info.lindblad.pedanticpadlock.asset

object StatusBadgeService {

  lazy val colors = Map(
    "green" -> ColorCombination("#5ACA24", "#4EBC13"),
    "amber" -> ColorCombination("#FEC113", "#E2AB00"),
    "red" -> ColorCombination("#FF3328", "#DC1813")
  )

  // Possible values: A+, A-, A-F, T (no trust) and M (certificate name mismatch)
  lazy val badges = Map(
    "A" -> new StatusBadge("A", colors.get("green")),
    "A-" -> new StatusBadge("A-", colors.get("green")),
    "A+" -> new StatusBadge("A+", colors.get("green")),
    "B" -> new StatusBadge("B", colors.get("amber")),
    "C" -> new StatusBadge("C", colors.get("amber")),
    "D" -> new StatusBadge("C", colors.get("amber")),
    "E" -> new StatusBadge("E", colors.get("amber")),
    "F" -> new StatusBadge("F", colors.get("red")),
    "T" -> new StatusBadge("T", colors.get("red")),
    "M" -> new StatusBadge("M", colors.get("red"))
  )

}
