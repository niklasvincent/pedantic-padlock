package info.lindblad.pedanticpadlock.directives

import spray.http.MediaTypes._
import spray.routing.{HttpService, Route}

trait HealthCheckDirective { this: HttpService =>

  val healthCheck: Route =
    get {
      pathPrefix("management") {
        path("healthcheck") {
          respondWithMediaType(`application/json`) {
            complete( """{ "status": "ok" }""")
          }
        }
      }
    }
}
