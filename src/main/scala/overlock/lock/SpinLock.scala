package overlock.lock

import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Dietrich Featherston
 */
trait SpinLock {
  val count = new AtomicInteger(0)
  /**
   * hold a counter open while performing a thunk
   */
  def run(op: => Unit) {
    count.incrementAndGet
    try {
      op
    }
    finally {
      count.getAndDecrement
    }
  }
  /**
   * wait for counters to clear
   */
  def spin = while(count.get() > 0) Thread.`yield`
}
