package overlock.lock
import java.util.concurrent.locks.ReentrantReadWriteLock

class Lock {
  val lock = new ReentrantReadWriteLock
  
  def readLock[T](f : => T) : T = {
    lock.readLock.lock
    try {
      f
    } finally {
      lock.readLock.unlock
    }
  }
  
  def writeLock[T](f : => T) : T = {
    lock.writeLock.lock
    try {
      f
    } finally {
      lock.writeLock.unlock
    }
  }
}