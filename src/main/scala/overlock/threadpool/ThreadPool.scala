package overlock.threadpool

import java.util.concurrent._

object ThreadPool {
  /**
   * Makes the equivalent of a cached thread pool from {@link Executors}
   */
  def instrumentedCached(path : String, name : String) : Executor = {
    new InstrumentedThreadPoolExecutor(path,
      name,
      0, 
      Int.MaxValue, 
      60l, 
      TimeUnit.SECONDS,
      new SynchronousQueue[Runnable],
      new NamedThreadFactory(name),
      new ThreadPoolExecutor.AbortPolicy)
  }
  
  def instrumentedFixed(path : String, name : String, n : Int) : Executor = {
    new InstrumentedThreadPoolExecutor(path,
      name,
      n,
      n,
      60l,
      TimeUnit.SECONDS,
      new LinkedBlockingQueue[Runnable],
      new NamedThreadFactory(name),
      new ThreadPoolExecutor.AbortPolicy)
  }
  
  def instrumentedElastic(path : String, name : String, coreSize : Int, maxSize : Int) : Executor = {
    val queue = new ElasticBlockingQueue[Runnable]
    val pool = new InstrumentedThreadPoolExecutor(path,
      name,
      coreSize,
      maxSize,
      60l,
      TimeUnit.SECONDS,
      queue,
      new NamedThreadFactory(name),
      new ThreadPoolExecutor.AbortPolicy)
    queue.executor = pool
    pool
  }
}