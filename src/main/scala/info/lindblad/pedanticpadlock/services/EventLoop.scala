package info.lindblad.pedanticpadlock.services

import info.lindblad.pedanticpadlock.model._
import info.lindblad.pedanticpadlock.util.Logging

object EventLoop extends Logging {
  def tick(scanStateStorage: ScanStateStorage, scanStateProcessor: ScanStateProcessing, currentTime: Long, validDuration: Long): Unit = {
    logger.info(s"Executing background processing")

    scanStateStorage.getAll.map(entry => {
        scanStateStorage.put(entry._1, scanStateProcessor.process(entry._2, currentTime, validDuration))
    })

    logger.info("Done executing background processing")
  }

}
