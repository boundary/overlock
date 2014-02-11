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

package overlock.atomicmap

import java.util.concurrent.{ConcurrentNavigableMap, ConcurrentSkipListMap}
import java.util.{NavigableMap, NavigableSet}

class AtomicNavigableMap[A,B](u : => ConcurrentNavigableMap[A,Any]) extends AtomicMap[A,B](u) {

  override val under = u
  def comparator = under.comparator
  
  override def empty = new AtomicNavigableMap[A,B](u)
  
  def subMap(fromKey : A, fromInclusive : Boolean, toKey : A, toInclusive : Boolean) : AtomicNavigableMap[A,B] = {
    new AtomicNavigableMap(under.subMap(fromKey,fromInclusive,toKey,toInclusive))
  }
}
