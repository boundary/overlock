package threadpool

import java.util.concurrent._
import atomic._

/**
 * Based off of the NamedThreadFactory in cassandra
 */
class NamedThreadFactory(val name : String, val priority : Int = Thread.NORM_PRIORITY) extends ThreadFactory {
  val counter = new AtomicInteger(0)
  
  override def newThread(r : Runnable) : Thread = {
    val threadName = name + ":" + counter.getAndIncrement
    val t = new Thread(r, threadName)
    t.setPriority(priority)
    t
  }
}