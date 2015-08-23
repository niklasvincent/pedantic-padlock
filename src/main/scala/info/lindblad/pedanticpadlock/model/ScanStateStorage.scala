package info.lindblad.pedanticpadlock.model

import java.util.concurrent.ConcurrentHashMap

import com.redis.RedisClient
import info.lindblad.pedanticpadlock.util.{Logging, JsonUtil}
import org.json4s.JsonAST._

import scala.collection.JavaConversions._
import scala.collection.immutable.::
import scala.collection.mutable.ListBuffer

/**
 * Generic storage trait for scan states
 *
 * Can be used to implement in memory storage
 * or as a proxy for database calls
 */
trait ScanStateStorage {

  /**
   * Get scan state for domain name
   * @param domainName
   * @return Option[ScanState]
   */
  def get(domainName: String): Option[ScanState]

  /**
   * Get scan state for domain name if it exists
   *
   * If the scan state does not exist, add it as unprocessed
   *
   * @param domainName
   * @return Option[ScanState]
   */
  def getOrPut(domainName: String): Option[ScanState]

  /**
   * Update scan state for domain name
   * @param domainName
   * @param scanState
   */
  def put(domainName: String, scanState: ScanState): Unit

  /**
   * Add domain name
   * @param domainName
   */
  def add(domainName: String): Unit

  /**
   * Get all domain names and their current scan state
   * @return allScanStates
   */
  def getAll(): Seq[(String, ScanState)]

}

/**
 * Adheres to the above storage trait and uses a concurrent hash map
 * @param scanStateMap
 */
class InMemoryScanStateStorage(scanStateMap: ConcurrentHashMap[String, ScanState]) extends ScanStateStorage {

  def get(domainName: String): Option[ScanState] = Option(scanStateMap.get(domainName))

  def getOrPut(domainName: String): Option[ScanState] = {
    get(domainName) match {
      case Some(scanState) => Some(scanState)
      case _ => {
        put(domainName, new NotProcessed(domainName))
        None
      }
    }
  }

  def put(domainName: String, scanState: ScanState) = scanStateMap.put(domainName, scanState)

  def add(domainName: String) = scanStateMap.putIfAbsent(domainName, new NotProcessed(domainName))

  def getAll: Seq[(String, ScanState)] = scanStateMap.entrySet.map(entry => (entry.getKey, entry.getValue)).toList
}

/**
 * Adheres to the above storage trait and uses Redis
 * @param redisClient
 */
class RedisScanStateStorage(redisClient: RedisClient) extends ScanStateStorage with Logging {

  def get(domainName: String): Option[ScanState] = {
    redisClient.get(domainName) match {
      case Some(scanStateJson) => {
        JsonUtil.fromJSONOption[ScanState](scanStateJson)
      }
    }
  }

  def getOrPut(domainName: String): Option[ScanState] = {
    redisClient.get(domainName) match {
      case Some(scanStateJson) => JsonUtil.fromJSONOption[ScanState](scanStateJson)
      case _ => {
        put(domainName, new NotProcessed(domainName))
        None
      }
    }
  }

  def put(domainName: String, scanState: ScanState) = {
    redisClient.set(domainName, JsonUtil.toJSON(scanState))
  }

  def add(domainName: String) = {
    redisClient.setnx(domainName,JsonUtil.toJSON(new NotProcessed(domainName)))
  }

  def getAll: Seq[(String, ScanState)] = {
    val scanStateEntries = new ListBuffer[(String, ScanState)]()
    redisClient.keys() match {
      case Some(listOfKeys) => {
        listOfKeys.foreach(key => {
          key match {
            case Some(validKey) => {
              get(validKey) match {
                case Some(scanState) => scanStateEntries.append((validKey, scanState))
              }
            }
          }
        })
      }
    }
    scanStateEntries.toList
  }

}