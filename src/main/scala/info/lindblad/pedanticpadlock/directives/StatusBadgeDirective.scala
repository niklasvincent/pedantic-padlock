package info.lindblad.pedanticpadlock.directives

import info.lindblad.pedanticpadlock.info.lindblad.pedanticpadlock.asset.{StatusBadgeService, StatusBadge}
import spray.http.CacheDirectives.`max-age`
import spray.http.HttpHeaders.{RawHeader, `Cache-Control`}
import spray.http.MediaTypes._
import spray.routing.{Route, Directives}

import spray.caching.{ExpiringLruCache, Cache}

import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class StatusBadgeDirective extends Directives {

  implicit val ec = ExecutionContext.Implicits.global

  lazy val cache: Cache[StatusBadge] = new ExpiringLruCache(1000, 100, 4.hours, 2.hours)

  def cachedBadge(domain: String): Future[StatusBadge] = cache(domain) {
    StatusBadgeService.generateBadge(domain).get
  }

  val noCacheHeaders = List(
    RawHeader("Cache-Control", "no-cache, no-store, must-revalidate"),
    RawHeader("Pragma", "no-cache"),
    RawHeader("Expires", "0")
  )

  lazy val badgeRoute: Route = pathPrefix("status") {
    path("badge") {
      requestUri { requestUri =>
        parameters("domain") { domain =>
          respondWithHeaders(noCacheHeaders) {
            respondWithMediaType(`image/svg+xml`) {
              onComplete(cachedBadge(domain)) {
                case Success(badge) => complete(badge.toString)
                case Failure(_) => complete(StatusBadgeService.default.toString)
              }
            }
          }
        }
      }
    }
  }

  lazy val routes = badgeRoute

}