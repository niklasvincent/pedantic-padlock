package info.lindblad.pedanticpadlock.model

import java.util.concurrent.ConcurrentHashMap

import scala.collection.JavaConversions._

trait ScanStateStorage {

  def get(domainName: String): Option[ScanState]

  def getOrPut(domainName: String): Option[ScanState]

  def put(domainName: String, scanState: ScanState): Unit

  def add(domainName: String): Unit

  def getAll(): Seq[(String, ScanState)]

}

class InMemoryScanStateStorage(val scanStateMap: ConcurrentHashMap[String, ScanState]) extends ScanStateStorage {

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