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

import java.util.concurrent._
import locks._

object OneShotThunk {
  def unapply[A](ost : OneShotThunk[A]) : Option[A] = {
    ost.value match {
      case v : A => Some(v)
      case _ => None
    }
  }
}

class OneShotThunk[A](op : => A) {
  lazy val value = op
  
  override def equals(other : Any) = other match {
    case o : OneShotThunk[_] => value == o.value
    case v : A => value == v
    case _ => false
  }
}