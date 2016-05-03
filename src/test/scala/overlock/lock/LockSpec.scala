package overlock.lock

import org.specs2.mutable._
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

class LockSpec extends Specification {
  "Lock" should {
    "acquire a lock with tryLock if it's not already held" in {
      val lock = new Lock

      lock.tryWriteLock {
        true
      }.get must beTrue
    }

    "not acquire a lock with tryLock if it's already held" >> {
      val done = new AtomicBoolean(false)
      val lock = new Lock
      val holdUp = new CountDownLatch(1)

      val dontLetGo = new Thread {
        override def run() {
          lock.writeLock {
            holdUp.countDown()
            while (!done.get()) {  }
          }
        }
      }
      dontLetGo.start()
      holdUp.await()

      try {
        lock.tryWriteLock {
          throw new RuntimeException("Shouldn't be able to get here")
        }.get must beFalse
      } finally {
        done.set(true)
      }
    }

    "be able to be held by multiple readers" >> {
      val numReaders = 5
      val done = new AtomicBoolean(false)
      val holdUp = new CountDownLatch(numReaders)
      val lock = new Lock

      val readers =
        for (n <- 0 until numReaders) yield new Thread {
          override def run() {
            lock.tryReadLock {
              holdUp.countDown()
              while (!done.get) { }
            }.orElse {
              ko("I want to lock but it didn't let me")
            }
          }
        }

      readers.foreach(t => t.start())
      holdUp.await()
      lock.tryWriteLock {
        ko("Shouldn't be able to write lock")
      }.get must beFalse
    }

    "be able to held by a single writer" >> {
      val holdUp = new CountDownLatch(1)
      val done = new AtomicBoolean(false)
      val lock = new Lock

      val writer = new Thread {
        override def run() {
          lock.tryWriteLock {
            holdUp.countDown()
            while (!done.get) { }
          }.orElse {
            ko("Couldn't acquire a write lock")
          }
        }
      }

      writer.start()
      holdUp.await()

      lock.tryReadLock {
        false
      }.get must beFalse
    }
  }
}
