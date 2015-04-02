package overlock.threadpool

import org.specs2.mutable._

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
      pool.getPoolSize must beEqualTo(2)
    }
    
    "switch to queueing behavior" in {
      val pool = ThreadPool.instrumentedElastic("threadpool", "3", 1, 2).asInstanceOf[InstrumentedThreadPoolExecutor]
      for (i <- (0 until 5)) {
        pool.execute(new Runnable {
          def run = Thread.sleep(100)
        })
      }
      pool.getPoolSize must beEqualTo(2)
      pool.getQueue.size must beEqualTo(3)
    }
  }
}
