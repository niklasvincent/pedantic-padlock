import info.lindblad.pedanticpadlock.model.{ScanReport, FinishedResult}
import org.scalatest.{Matchers, FlatSpec}

class ScanResultExpirySpec extends FlatSpec with Matchers {

  it should "expire after a given duration" in {
   val scanState = new FinishedResult("example.com", ScanReport(canConnect = true))
   val inThirtyFiveMinutes = System.currentTimeMillis() + 35 * 60 * 1000
   val thirtyMinutes = 30 * 60 * 1000L

   scanState.isExpired(inThirtyFiveMinutes, thirtyMinutes) should be (true)
  }

  it should "not expire prematurely" in {
    val scanState = new FinishedResult("example.com", ScanReport(canConnect = true))
    val inTwentyFiveMinutes = System.currentTimeMillis() + 25 * 60 * 1000
    val thirtyMinutes = 30 * 60 * 1000L

    scanState.isExpired(inTwentyFiveMinutes, thirtyMinutes) should be (false)
  }

}