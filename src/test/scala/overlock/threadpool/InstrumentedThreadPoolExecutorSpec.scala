package overlock.threadpool

import org.specs2.mutable._
import java.util.concurrent.atomic._

class InstrumentedThreadPoolExecutorSpec extends Specification {
  "InstrumentedThreadPoolExecutor" >> {
    "basically work" >> {
      val pool = ThreadPool.instrumentedFixed("threadpool", "1", 1).asInstanceOf[InstrumentedThreadPoolExecutor]
      val counter = new AtomicInteger(0)
      pool.execute(new Runnable {
        def run {
          counter.getAndIncrement
        }
      })
      Thread.sleep(100)
      counter.get mustEqual 1
      pool.threadGauge.value mustEqual 1
      pool.queueGauge.value mustEqual 0
      pool.requestRate.count mustEqual 1
      pool.rejectedRate.count mustEqual 0
      pool.executionTimer.count mustEqual 1
      pool.activeThreadGauge.value mustEqual 0
    }
  }
}
