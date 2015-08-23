import info.lindblad.pedanticpadlock.model._
import info.lindblad.pedanticpadlock.util.JsonUtil
import org.scalatest.{FlatSpec, Matchers}

class JsonUtilSpec extends FlatSpec with Matchers {

  "JsonUtil" should "deserialize/serialize NotProcessed correctly" in {
    val notProcessedJson = JsonUtil.toJSON(new NotProcessed("niklaslindblad.se"))
    val notProcessed = JsonUtil.fromJSONOption[ScanState](notProcessedJson).get
    notProcessed.isInstanceOf[NotProcessed] should be(true)
  }

  it should "deserialize/serialzie AwaitingResult correctly" in {
    val awaitingResultJson = JsonUtil.toJSON(new AwaitingResult("niklaslindblad.se", ScanReport(canConnect = true)))
    val awaitingResult = JsonUtil.fromJSONOption[ScanState](awaitingResultJson).get
    awaitingResult.isInstanceOf[AwaitingResult] should be(true)
  }

  it should "deserialize/serialize FinishedResult correctly" in {
    val finishedResultJson = JsonUtil.toJSON(new FinishedResult("niklaslindblad.se", ScanReport(canConnect = true, sslLabsReport = Some(SslLabsReport("A+", false, false, false, 2048, "SHA")))))
    val finishedResult = JsonUtil.fromJSONOption[ScanState](finishedResultJson).get
    finishedResult.isInstanceOf[FinishedResult] should be(true)
  }

  it should "preserve creation timestamp correctly" in {
    val finishedResultToBeSerialized = new FinishedResult("niklaslindblad.se", scanReport = ScanReport(canConnect = true, sslLabsReport = Some(SslLabsReport("A+", false, false, false, 2048, "SHA"))))
    val finishedResultJson = JsonUtil.toJSON(finishedResultToBeSerialized)
    val finishedResultDeserialized: FinishedResult = JsonUtil.fromJSONOption[FinishedResult](finishedResultJson).get
    finishedResultDeserialized.timeCreated should be (finishedResultToBeSerialized.timeCreated)
  }

}