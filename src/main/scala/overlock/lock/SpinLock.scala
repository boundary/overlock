//
// Copyright 2011, Boundary
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package overlock.lock

import java.util.concurrent.atomic._

/**
 * @author Dietrich Featherston
 * @author Cliff Moon
 */
trait SpinLockable {
  val spinlock = new SpinLock
}
 
class SpinLock {
  val writer = new AtomicBoolean(false)
  val count = new AtomicInteger(0)
  
  /**
   * hold a counter open while performing a thunk
   */
  def readLock[A](op: => A) : A = {
    waitWriter //wait if a writer has acquired the lock
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
  def waitReaders = while(count.get > 0) {}
  
  def waitWriter = while(writer.get) {} 
  
  def writeLock[A](op : => A) : A = {
    if (!writer.compareAndSet(false,true)) { //lost the write race, start over
      write(op)
    }
    waitReaders  //wait for all of the readers to clear
    try {
      op
    } finally {
      writer.set(false)
    }
  }
}
