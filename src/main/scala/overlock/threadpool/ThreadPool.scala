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

object ThreadPool {
  /**
   * Makes the equivalent of a cached thread pool from {@link Executors}
   */
  def instrumentedCached(path : String, name : String) : Executor = {
    new InstrumentedThreadPoolExecutor(path,
      name,
      0, 
      Int.MaxValue, 
      60l, 
      TimeUnit.SECONDS,
      new SynchronousQueue[Runnable],
      new NamedThreadFactory(name))
  }
  
  def instrumentedFixed(path : String, name : String, n : Int) : Executor = {
    new InstrumentedThreadPoolExecutor(path,
      name,
      n,
      n,
      60l,
      TimeUnit.SECONDS,
      new LinkedBlockingQueue[Runnable],
      new NamedThreadFactory(name))
  }
  
  def instrumentedElastic(path : String, name : String, coreSize : Int, maxSize : Int) : Executor = {
    val queue = new ElasticBlockingQueue[Runnable]
    val pool = new InstrumentedThreadPoolExecutor(path,
      name,
      coreSize,
      maxSize,
      60l,
      TimeUnit.SECONDS,
      queue,
      new NamedThreadFactory(name))
    queue.executor = pool
    pool
  }
}