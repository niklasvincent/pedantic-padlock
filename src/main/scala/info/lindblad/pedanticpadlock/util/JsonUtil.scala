package info.lindblad.pedanticpadlock.util

import info.lindblad.pedanticpadlock.model._
import org.json4s._
import org.json4s.ShortTypeHints
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, write}

import scala.util.Try

object JsonUtil {

  implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[NotProcessed], classOf[AwaitingResult], classOf[FinishedResult], classOf[ScanReport], classOf[SslLabsReport])))

  def toJSON(objectToWrite: AnyRef): String = write(objectToWrite)

  def fromJSONOption[T](jsonString: String)(implicit mf: Manifest[T]): Option[T] = {
    Try(read[T](jsonString)).toOption
  }

}