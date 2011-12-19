package overlock.lock

import org.specs._
import java.util.{concurrent => juc}
import juc.atomic._
import java.util.concurrent.CountDownLatch

class SpinLockSpec extends SpecificationWithJUnit {
  "SpinLock" should {
    "lock out writers while there are critical readers" in {

      val numReaders = 1
      val numWriters = 1
      val latch = new CountDownLatch(numReaders+numWriters)

      // count number of total reads and writes
      val writes  = new AtomicInteger(0)
      val reads   = new AtomicInteger(0)

      // flag gets tripped if a writer observes a reader in the critical section
      val rwFlag = new AtomicBoolean(false)
      // flag gets tripped if a reader observes a writer in the critical section
      val wrFlag = new AtomicBoolean(false)
      // flag gets tripped if a writer observes another writer in the critical section
      val wwFlag = new AtomicBoolean(false)

      // readers set this when they enter the CS
      val readersInCS = new AtomicInteger(0)
      // writers set this when they enter the CS
      val writersInCS  = new AtomicInteger(0)

      val lock = new SpinLock

      val readers =
        for(n <- 0 until numReaders) yield
          new Thread {
            override def run {
              lock.readLock {
                // allow readers to enter the critical section first
                latch.countDown
                readersInCS.incrementAndGet
                try {
                  // check for invalid state
                  // observed a writer, ruh roh!
                  if(writersInCS.get > 0) {
                    rwFlag.set(true)
                    println("ruh roh")
                    println("readers = " + readersInCS.get)
                  }
                  // do the main unit of work
                  reads.getAndIncrement
                  Thread.sleep(10)
                }
                finally {
                  readersInCS.getAndDecrement
                }
              }
            }
          }

      val writers =
        for(n <- 0 until numWriters) yield
          new Thread {
            override def run {
              latch.countDown
              lock.writeLock {
                try {
                  // check for invalid state
                  if(writersInCS.getAndIncrement > 0) wwFlag.set(true)
                  if(readersInCS.get > 0) wrFlag.set(true)
                  // do main unit of work
                  writes.getAndIncrement
                  Thread.sleep(10)
                }
                finally {
                  writersInCS.getAndDecrement
                }
              }
            }
          }

      readers.foreach(_.start)
      writers.foreach(_.start)

      readers.foreach(_.join)
      writers.foreach(_.join)

      writes.get must ==(numWriters)
      reads.get must ==(numReaders)

      writersInCS.get must ==(0)
      readersInCS.get must ==(0)
      rwFlag.get must ==(false)
      wrFlag.get must ==(false)
      wwFlag.get must ==(false)
    }
    
    "lock out multiple writers" in {

      val numWriters = 4

      // counter of writes
      val writes = new AtomicInteger(0)
      // is there a writer in the CS
      val writerInCS = new AtomicBoolean(false)
      // writers trip this flag if they enter the CS while writerInCS == true
      val csViolated = new AtomicBoolean(false)

      val latch = new CountDownLatch(numWriters)

      val lock = new SpinLock
      val threads = for (n <- (0 until numWriters)) yield {
        new Thread {
          override def run {
            // do our best to force contention with a latch
            latch.countDown
            lock.writeLock {
              // check the flag first thing
              if(writerInCS.get) csViolated.set(true)
              writerInCS.set(true)
              writes.getAndIncrement
              Thread.sleep(10) // it's a free country bro
              writerInCS.set(false)
            }
          }
        }
      }
      
      threads.foreach(_.start)
      threads.foreach(_.join)

      writes.get must ==(numWriters)
      writerInCS.get must ==(false)
      csViolated.get must ==(false)
    }
  }
}