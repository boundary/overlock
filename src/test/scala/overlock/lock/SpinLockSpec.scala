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
          lock.read {
            reads.getAndIncrement
            Thread.sleep(10)
          }
        }
      }
      val writerThread = new Thread {
        override def run {
          lock.write {
            writes.getAndIncrement
            Thread.sleep(10)
          }
        }
      }
      
      readerThread.start
      writerThread.start
      writes.get must ==(0)
      reads.get must ==(1)
      readerThread.join
      writerThread.join
      writes.get must ==(1)
    }
  }
}