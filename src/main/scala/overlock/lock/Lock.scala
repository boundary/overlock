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

  def tryLock[T](f : => T) : LockResult = tryWriteLock(f)

  def tryReadLock[T](f : => T) : LockResult = {
    if (lock.readLock().tryLock()) {
      try {
        f
        LockResult.TRUE
      } finally {
        lock.readLock.unlock
      }
    } else {
      LockResult.FALSE
    }
  }

  def tryWriteLock[T](f : => T) : LockResult = {
    if (lock.writeLock().tryLock()) {
      try {
        f
        LockResult.TRUE
      } finally {
        lock.writeLock.unlock
      }
    } else {
      LockResult.FALSE
    }
  }
}

sealed class LockResult(private val success: Boolean) {
  def orElse[U](f : => U) {
    if (!success) {
      f
    }
  }

  def get: Boolean = success
}

object LockResult {
  val TRUE = new LockResult(true)
  val FALSE = new LockResult(false)
}

