package overlock.threadpool

import org.specs2.mutable._
import java.util.concurrent._

class ElasticBlockingQueueSpec extends Specification {
  "ElasticBlockingQueue" >> {
    "make a thread pool expand" >> {
      val pool = ThreadPool.instrumentedElastic("threadpool", "2", 1, 5).asInstanceOf[InstrumentedThreadPoolExecutor]
      pool.execute(new Runnable {
        def run = Thread.sleep(100)
      })
      pool.execute(new Runnable {
        def run = Thread.sleep(100)
      })
      pool.getPoolSize mustEqual 2
    }

    "switch to queueing behavior" >> {
      val pool = ThreadPool.instrumentedElastic("threadpool", "3", 1, 2).asInstanceOf[InstrumentedThreadPoolExecutor]
      for (i <- (0 until 5)) {
        pool.execute(new Runnable {
          def run = Thread.sleep(100)
        })
      }
      pool.getPoolSize mustEqual 2
      pool.getQueue.size mustEqual 3
    }
  }
}
