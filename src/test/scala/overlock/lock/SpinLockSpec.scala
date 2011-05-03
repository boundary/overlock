package overlock.lock

import org.specs._
import java.util.{concurrent => juc}
import juc.atomic._

class SpinLockSpec extends Specification {
  "SpinLock" should {
    "lock out writers while there are critical readers" in {
      val writes = new AtomicInteger(0)
      val reads = new AtomicInteger(0)
      
      val lock = new SpinLock
      val readerThread = new Thread {
        override def run {
          lock.readLock {
            reads.getAndIncrement
            Thread.sleep(10)
          }
        }
      }
      val writerThread = new Thread {
        override def run {
          lock.writeLock {
            writes.getAndIncrement
            Thread.sleep(10)
          }
        }
      }
      
      readerThread.start
      writerThread.start
      Thread.sleep(1)
      writes.get must ==(0)
      reads.get must ==(1)
      readerThread.join
      writerThread.join
      writes.get must ==(1)
    }
    
    "lock out multiple writers" in {
      val writes = new AtomicInteger(0)
      
      val lock = new SpinLock
      val threads = for (n <- (0 to 1)) yield {
        new Thread {
          override def run {
            lock.writeLock {
              writes.getAndIncrement
              Thread.sleep(10)
            }
          }
        }
      }
      
      threads.foreach(_.start)
      Thread.sleep(1)
      writes.get must ==(1)
      threads.foreach(_.join)
      writes.get must ==(2)
    }
  }
}