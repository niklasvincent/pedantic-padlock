package info.lindblad.pedanticpadlock.info.lindblad.pedanticpadlock.services

import akka.actor.ActorSystem
import akka.util.Timeout
import info.lindblad.pedanticpadlock.util.Logging
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import spray.http._
import spray.client.pipelining._

import net.liftweb.json._

import scala.util.Success

class QualysService(apiBaseUrl: String = "https://api.ssllabs.com/api/", apiVersion: String = "2") extends Logging {

  implicit val formats = DefaultFormats

  implicit val timeout = Timeout(300 seconds)

  private implicit val system = ActorSystem("qualys-api-client")
  private implicit val dispatcher = system.dispatcher

  val sendWithUserAgent: HttpRequest => Future[String] =
    (addHeader("User-Agent", s"PedanticPadlock")
      ~> logRequest { req =>
      logger.debug("Qualys SSL Labs API Request ({}:\n{}\n{}\n{}", req.method, req.uri, req.headers.toString, req.entity.asString)
    }
      ~> sendReceive
      ~> unmarshal[String])

  lazy val apiUrl = s"${apiBaseUrl}v${apiVersion}/"

  def analyze(host: String) = {
    for {
      analysisJson <- sendWithUserAgent(HttpRequest(HttpMethods.GET, Uri(s"${apiUrl}analyze?host=${host}&publish=off&fromCache=on&maxAge=4&all=on&ignoreMismatch=off")))
    } yield parse(analysisJson)
  }

  def gradeIfReady(host: String): Option[String] = {
    Await.ready(analyze(host), Duration.Inf).value match {
      case Some(Success(analysis: JValue)) => {
        (analysis \ "status").extract[String] match {
          case "READY" => {
            logger.debug("Report for {} ready", host)
            val grade = (analysis \ "endpoints" \ "grade") match {
              case gradeJson: JString => gradeJson.extract[String]
              case gradeJson: JArray => {
                val grades = new scala.collection.mutable.ListBuffer[String]()
                for ( g <- gradeJson.children ) grades += g.extract[String]
                grades.sorted.reverse.head
              }
              case _ => {
                logger.debug("Could not extract grade, giving an F")
                "F"
              }
            }

            logger.debug("Qualsys reported grade {}", grade)
            Some(grade)
          }
          case _ => {
            logger.debug("Report for {} not ready", host)
            // Need to pull again
            None
          }
        }
      }
    }
  }

}

object QualysService {

  def apply() = new QualysService()

}