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
import jsr166y.LinkedTransferQueue

class ElasticBlockingQueue[A] extends LinkedTransferQueue[A] {
  var executor : ThreadPoolExecutor = null
  
  override def offer(item : A) : Boolean = {
    val left = executor.getMaximumPoolSize - executor.getPoolSize
    if (!tryTransfer(item)) {
      if (left > 0) {
        false
      } else {
        super.offer(item)
      }
    } else {
      true
    }
  }
}