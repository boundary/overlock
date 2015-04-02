package overlock.lock

import org.specs2.mutable._
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

class LockSpec extends SpecificationWithJUnit {
  "Lock" should {
    "acquire a lock with tryLock if it's not already held" in {
      val lock = new Lock

      lock.tryWriteLock(()) must beEqualTo(LockResult.TRUE)
    }

    "not acquire a lock with tryLock if it's already held" in {
      val done = new AtomicBoolean(false)
      val lock = new Lock
      val holdUp = new CountDownLatch(1)

      val dontLetGo = new Thread {
        override def run() {
          lock.writeLock {
            holdUp.countDown()
            while (!done.get()) { /*do nothing*/ }
          }
        }
      }
      dontLetGo.start()
      holdUp.await()

      try {
        lock.tryWriteLock(()) must beEqualTo(LockResult.FALSE)
      } finally {
        done.set(true)
      }
    }

    "be able to be held by multiple readers" in {
      val numReaders = 5
      val done = new AtomicBoolean(false)
      val holdUp = new CountDownLatch(numReaders)
      val lock = new Lock

      val readers =
        for (n <- 0 until numReaders) yield new Thread {
          override def run() {
            lock.tryReadLock {
              holdUp.countDown()
              while (!done.get) { /*spin, spin, spin*/}
            }.orElse {
              failure("I want to lock but it didn't let me")
            }
          }
        }

      readers.foreach(t => t.start())
      holdUp.await()
      lock.tryWriteLock(()) must beEqualTo(LockResult.FALSE)
    }

    "be able to held by a single writer" in {
      val holdUp = new CountDownLatch(1)
      val done = new AtomicBoolean(false)
      val lock = new Lock

      val writer = new Thread {
        override def run() {
          lock.tryWriteLock {
            holdUp.countDown()
            while (!done.get) { /*keep on swimming, keep on swimming*/ }
          }.orElse {
            failure("Couldn't acquire a write lock")
          }
        }
      }

      writer.start()
      holdUp.await()

      lock.tryWriteLock(()) must beEqualTo(LockResult.FALSE)
    }
  }
}
