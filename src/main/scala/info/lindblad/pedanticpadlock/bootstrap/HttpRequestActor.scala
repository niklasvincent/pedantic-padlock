package info.lindblad.pedanticpadlock.bootstrap

import info.lindblad.pedanticpadlock.directives.{StatusBadgeDirective, HealthCheckDirective}
import info.lindblad.pedanticpadlock.util.Logging
import spray.http.StatusCodes._
import spray.http._
import spray.routing._
import spray.routing.directives.LoggingMagnet

class HttpRequestActor extends HttpServiceActor with HealthCheckDirective with Logging {

  override def actorRefFactory = context

  def requestMethodAndResponseStatusAsInfo(req: HttpRequest): Any => Unit = {
    case res: HttpResponse => logger.info("{} {} {}", req.method, req.uri, res.message.status.intValue.toString)
    case _ =>
  }

  def routeWithLogging = logRequestResponse(LoggingMagnet(requestMethodAndResponseStatusAsInfo _))(route)

  implicit val rejectionHandler = RejectionHandler {
    case MissingQueryParamRejection(paramName) :: _ => ctx => {
      val statusCode = NotFound
      ctx.complete(statusCode, "Request is missing required query parameter '" + paramName + '\'')
    }
    case x if RejectionHandler.Default.isDefinedAt(x) => ctx => {
      RejectionHandler.Default(x) {
        ctx.withHttpResponseMapped {
          case resp@HttpResponse(statusCode, _, _, _) => {
            resp
          }
        }
      }
    }
  }

  def handleTimeouts: Receive = {
    case Timedout(request: HttpRequest) => {
      sender() ! HttpResponse(StatusCodes.InternalServerError, "Too late")
      logger.info("{} timed out", request.uri.path)
    }
  }

  override def receive: Receive = runRoute(routeWithLogging)

  val route =
      new StatusBadgeDirective().routes ~
      healthCheck

}
