package info.lindblad.pedanticpadlock.directives

import java.util.concurrent.{TimeUnit, ConcurrentHashMap}
import info.lindblad.pedanticpadlock.asset.StatusBadge
import info.lindblad.pedanticpadlock.bootstrap.{Main, Configuration}
import info.lindblad.pedanticpadlock.model.{ScanStateStorage, ScanState}
import info.lindblad.pedanticpadlock.services.{EventLoop, StatusBadgeService}
import info.lindblad.pedanticpadlock.util.DomainNameValidator
import spray.http.HttpHeaders.RawHeader
import spray.http.MediaTypes._
import spray.routing.{Route, Directives}

import spray.caching.{ExpiringLruCache, Cache}

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Failure, Success}

import spray.http.StatusCodes.BadRequest

class StatusBadgeDirective(scanStateStore: ScanStateStorage) extends Directives {

  implicit val ec = ExecutionContext.Implicits.global

  lazy val cache: Cache[StatusBadge] = new ExpiringLruCache(
    Configuration.values.getInt("badge.cache.max-capacity"),
    Configuration.values.getInt("badge.cache.initial-capacity"),
    Configuration.duration("badge.cache.time-to-live", TimeUnit.HOURS),
    Configuration.duration("badge.cache.time-to-idle", TimeUnit.HOURS)
  )

  def cachedBadge(domain: String): Future[StatusBadge] = cache(domain) {
    StatusBadgeService.generateBadgeFromScanResult(scanStateStore.getOrPut(domain)).get
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
          DomainNameValidator(domain) match {
            case Some(validDomain) => {
              respondWithHeaders(noCacheHeaders) {
                respondWithMediaType(`image/svg+xml`) {
                  onComplete(cachedBadge(domain)) {
                    case Success(badge) => complete(badge.toString)
                    case Failure(_) => complete(StatusBadgeService.default.toString)
                  }
                }
              }
            }
            case None => {
              complete(BadRequest)
            }
          }
        }
      }
    }
  }

  lazy val routes = badgeRoute

}