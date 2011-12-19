package overlock.threadpool

import org.specs._
import java.util.concurrent._

class ElasticBlockingQueueSpec extends SpecificationWithJUnit {
  "ElasticBlockingQueue" should {
    "make a thread pool expand" in {
      val pool = ThreadPool.instrumentedElastic("threadpool", "2", 1, 5).asInstanceOf[InstrumentedThreadPoolExecutor]
      pool.execute(new Runnable {
        def run = Thread.sleep(100)
      })
      pool.execute(new Runnable {
        def run = Thread.sleep(100)
      })
      pool.getPoolSize must ==(2)
    }
    
    "switch to queueing behavior" in {
      val pool = ThreadPool.instrumentedElastic("threadpool", "3", 1, 2).asInstanceOf[InstrumentedThreadPoolExecutor]
      for (i <- (0 until 5)) {
        pool.execute(new Runnable {
          def run = Thread.sleep(100)
        })
      }
      pool.getPoolSize must ==(2)
      pool.getQueue.size must ==(3)
    }
  }
}