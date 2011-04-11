package threadpool

import java.util.concurrent._
import com.yammer.metrics._
import core._

class InstrumentedThreadPoolExecutor(path : String,
    name : String, 
    corePoolSize : Int,
    maximumPoolSize : Int,
    keepAliveTime : Long,
    unit : TimeUnit,
    workQueue : BlockingQueue[Runnable],
    factory : ThreadFactory,
    handler : RejectedExecutionHandler) extends 
    ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,unit,workQueue,factory,handler) with 
    Instrumented {
  override protected lazy val metricsGroup = new MetricsGroup(NameBuilder(path, name))
  protected val requestRate = metrics.meter("request", "requests", TimeUnit.SECONDS)
  protected val rejectedRate = metrics.meter("rejected", "requests", TimeUnit.SECONDS)
  protected val executionTimer = metrics.timer("execution")
  protected val queueSize = metrics.gauge("queue size")(getQueue.size)
  protected val startTime = new ThreadLocal[Long]
  
  setRejectedExecutionHandler(new RejectedExecutionHandler {
    def rejectedExecution(r : Runnable, executor : ThreadPoolExecutor) {
      rejectedRate.mark
      handler.rejectedExecution(r,executor)
    }
  })
  
  override protected def beforeExecute(t : Thread, r : Runnable) {
    startTime.set(System.nanoTime)
  }
  
  override protected def afterExecute(r : Runnable, t : Throwable) {
    val duration = System.nanoTime - startTime.get
    executionTimer.update(duration, TimeUnit.NANOSECONDS)
  }
}