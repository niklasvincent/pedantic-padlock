import java.util.concurrent.ConcurrentHashMap

import info.lindblad.pedanticpadlock.model._
import info.lindblad.pedanticpadlock.services.{ScanService, ScanStateProcessing, EventLoop}
import org.scalatest.{FlatSpec, Matchers}

class EventLoopSpec extends FlatSpec with Matchers {

  object TestScanStateProcessor extends ScanStateProcessing {

    def process(currentState: ScanState, currentTime: Long, validDuration: Long, startScan: NotProcessed => ScanState = startScan, pollScan: AwaitingResult => ScanState = pollScan): ScanState = {
      ScanService.process(currentState, currentTime, validDuration, TestScanStateProcessor.startScan, TestScanStateProcessor.pollScan)
    }

    def startScan(notProcessed: NotProcessed): ScanState = new AwaitingResult(notProcessed.domainName, ScanReport(canConnect = true))

    def pollScan(awaitingResult: AwaitingResult): FinishedResult = new FinishedResult(awaitingResult.domainName, ScanReport(canConnect = true))

  }

  it should "process scan states each tick" in {
    val scanStateStorage = new InMemoryScanStateStorage(new ConcurrentHashMap[String, ScanState]())
    scanStateStorage.put("example.com", new NotProcessed("example.com"))

    EventLoop.tick(scanStateStorage, TestScanStateProcessor, System.currentTimeMillis() + 2000L, 1000L)
    scanStateStorage.get("example.com").get.isInstanceOf[AwaitingResult] should be(true)

    EventLoop.tick(scanStateStorage, TestScanStateProcessor, System.currentTimeMillis() + 3000L, 1000L)
    scanStateStorage.get("example.com").get.isInstanceOf[FinishedResult] should be(true)

    EventLoop.tick(scanStateStorage, TestScanStateProcessor, System.currentTimeMillis() + 4000L, 1000L)
    scanStateStorage.get("example.com").get.isInstanceOf[AwaitingResult] should be(true)
  }

}