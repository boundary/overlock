package threadpool

import java.util.concurrent._
import concurrent.forkjoin._

class ElasticBlockingQueue[A] extends LinkedTransferQueue[A] {
  var executor : ThreadPoolExecutor = null
  
  override def offer(item : A) : Boolean = {
    val left = executor.getMaximumPoolSize - executor.getPoolSize
    if (!tryTransfer(item)) {
      if (left > 0) {
        false
      } else {
        super.offer(item)
      }
    } else {
      true
    }
  }
}