package info.lindblad.pedanticpadlock.bootstrap

import info.lindblad.pedanticpadlock.directives.{StatusBadgeDirective, HealthCheckDirective}
import spray.http.StatusCodes._
import spray.http._
import spray.routing._

class HttpRequestActor extends HttpServiceActor with HealthCheckDirective {

  override def actorRefFactory = context

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

  override def receive: Receive = runRoute(routes)

  val routes =
      new StatusBadgeDirective().routes ~
      healthCheck

}
