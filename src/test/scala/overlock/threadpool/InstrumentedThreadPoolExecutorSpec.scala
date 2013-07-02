package overlock.threadpool

import org.specs._
import java.util.concurrent.atomic._

class InstrumentedThreadPoolExecutorSpec extends SpecificationWithJUnit {
  "InstrumentedThreadPoolExecutor" should {
    "basically work" in {
      val pool = ThreadPool.instrumentedFixed("threadpool", "1", 1).asInstanceOf[InstrumentedThreadPoolExecutor]
      val counter = new AtomicInteger(0)
      pool.execute(new Runnable {
        def run {
          counter.getAndIncrement
        }
      })
      Thread.sleep(100)
      counter.get must ==(1)
      pool.threadGauge.value must ==(1)
      pool.queueGauge.value must ==(0)
      pool.requestRate.count must ==(1)
      pool.rejectedRate.count must ==(0)
      pool.executionTimer.count must ==(1)
      pool.activeThreadGauge.value must ==(0)
    }
  }
}
