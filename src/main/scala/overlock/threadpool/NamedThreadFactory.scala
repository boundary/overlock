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
package overlock.threadpool

import java.util.concurrent._
import atomic._
import org.slf4j.LoggerFactory

/**
 * Based off of the NamedThreadFactory in cassandra
 */
class NamedThreadFactory(val name : String, val priority : Int = Thread.NORM_PRIORITY) extends ThreadFactory {
  val counter = new AtomicInteger(0)
  
  override def newThread(r : Runnable) : Thread = {
    val threadName = name + ":" + counter.getAndIncrement
    val t = new ErrorLoggedThread(r, threadName)
    t.setPriority(priority)
    t
  }
}

class ErrorLoggedThread(r : Runnable, threadName : String) extends Thread(r, threadName) {
  protected lazy val log = LoggerFactory.getLogger(getClass)
  override def run {
    try {
      super.run
    } catch {
      case e : Throwable => 
        log.error("Exception was thrown in thread " + threadName, e)
        throw e
    }
  }
}
