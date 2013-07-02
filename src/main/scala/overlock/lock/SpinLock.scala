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

import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * @author Dietrich Featherston
 * @author Cliff Moon
 */
trait SpinLockable {
  val spinlock = new SpinLock
}
 
class SpinLock {
  private[this] val lock = new ReentrantReadWriteLock
  
  /**
   * hold a counter open while performing a thunk
   */
  def readLock[A](op: => A) : A = {
    val readLock = lock.readLock()
    while (!readLock.tryLock()) {}
    try {
      op
    }
    finally {
      readLock.unlock()
    }
  }
  
  def writeLock[A](op : => A) : A = {
    val writeLock = lock.writeLock()
    while (!writeLock.tryLock()) {}
    try {
      op
    } finally {
      writeLock.unlock()
    }
  }
}
