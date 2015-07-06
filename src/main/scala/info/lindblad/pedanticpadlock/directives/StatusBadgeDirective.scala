package info.lindblad.pedanticpadlock.directives

import info.lindblad.pedanticpadlock.info.lindblad.pedanticpadlock.asset.{StatusBadgeService, StatusBadge}
import spray.http.CacheDirectives.`max-age`
import spray.http.HttpHeaders.`Cache-Control`
import spray.http.MediaTypes._
import spray.routing.Directives

class StatusBadgeDirective extends Directives {

  lazy val routes = pathPrefix("status") {
    path("badge") {
      parameters("domain") { domain =>
        respondWithMediaType(`image/svg+xml`) {
          complete(StatusBadgeService.badges.get("A+").get.toString)
        }
      }
    }
  }
}