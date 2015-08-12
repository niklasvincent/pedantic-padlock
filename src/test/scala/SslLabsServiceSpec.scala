import info.lindblad.pedanticpadlock.services.SslLabsService
import org.json4s.DefaultFormats
import org.json4s.JsonAST.{JArray, JString}
import org.scalatest.{Matchers, FlatSpec}
import scala.io.Source

import org.json4s.native.JsonMethods._

class SslLabsServiceSpec extends FlatSpec with Matchers {

  implicit val formats = DefaultFormats

  def loadJson(filename: String): String = Source.fromURL(getClass.getResource(filename)).mkString

  it should "get the correct lowest grade from a series of grades" in {
    SslLabsService().getLowestGrade(JArray(List("A", "C", "B", "A").map(JString.apply))) should be ("C")
  }

  it should "understand when a report is not ready" in {
    SslLabsService().interpretReportJson(parse(loadJson("/ssllabs-unfinished-scan.json"))) should be(None)
  }

  it should "understand when a report is ready" in {
    SslLabsService().interpretReportJson(parse(loadJson("/ssllabs-finished-scan.json"))) should not be(None)
  }

  it should "extract a grade from a finished report" in {
    val report = SslLabsService().interpretReportJson(parse(loadJson("/ssllabs-finished-scan.json")))
    report.get.grade should be("A")
  }

}