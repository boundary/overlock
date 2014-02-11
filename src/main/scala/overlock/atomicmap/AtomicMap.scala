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

import scala.collection.mutable.ConcurrentMap
import java.util.concurrent.{ConcurrentMap => JConcurrentMap, ConcurrentSkipListMap, ConcurrentHashMap}
import java.util.Comparator
import org.cliffc.high_scale_lib._

object AtomicMap {
  def atomicCSLM[A,B] : AtomicNavigableMap[A,B] = {
    new AtomicNavigableMap(new ConcurrentSkipListMap[A,Any])
  }
  
  def atomicCSLM[A,B](comp : Comparator[A]) : AtomicNavigableMap[A,B] = {
    new AtomicNavigableMap(new ConcurrentSkipListMap[A,Any](comp))
  }
  
  def atomicNBHM[A,B] : AtomicMap[A,B] = {
    new AtomicMap(new NonBlockingHashMap[A,Any])
  }
  
  def atomicNBHM[A,B](size : Int) : AtomicMap[A,B] = {
    new AtomicMap(new NonBlockingHashMap[A,Any](size))
  }
  
  def atomicCHM[A,B] : AtomicMap[A,B] = {
    new AtomicMap(new ConcurrentHashMap[A,Any])
  }
  
  def atomicCHM[A,B](cap : Int) : AtomicMap[A,B] = {
    new AtomicMap(new ConcurrentHashMap[A,Any](cap))
  }
  
  def atomicCHM[A,B](cap : Int, loadFactor : Float) : AtomicMap[A,B] = {
    new AtomicMap(new ConcurrentHashMap[A,Any](cap,loadFactor))
  }
  
  def atomicCHM[A,B](cap : Int, loadFactor : Float, concurrencyLevel : Int) : AtomicMap[A,B] = {
    new AtomicMap(new ConcurrentHashMap[A,Any](cap, loadFactor,concurrencyLevel))
  }
}

class AtomicMap[A,B](u : => JConcurrentMap[A,Any]) extends ConcurrentMap[A,B] {
  val under = u

  override def empty = new AtomicMap[A,B](u)
  
  override def getOrElseUpdate(key : A, op : => B) : B = {
    val t = new OneShotThunk(op)
    under.putIfAbsent(key, t) match {
      case null =>
        try {
          val ve = t.value
          under.replace(key, t, ve)
          //regardless of whether or not the replace worked, we must
          //return ve to remain within the bounds of the getOrElseUpdate
          // contract. If a concurrent update happened, this thread
          // ought not to see it until after returning from this call.
          ve
        } catch {
          case ex : Exception =>
            under.remove(key, t)
            throw ex
        }
      case OneShotThunk(v : B) => v
      case v : B => v
    }
  }
  
  def replace(key : A, value : B) : Option[B] = {
    Option(under.replace(key,value)).map {
      case b : B => b
      case OneShotThunk(v : B) => v
    }
  }
  
  /**
   * This works because ConcurrentMap defines it as .equals
   */
  def replace(key : A, oldVal : B, newVal : B) : Boolean = {
    val oldThunk = new OneShotThunk(oldVal)
    under.replace(key, oldThunk, newVal)
  }
  
  def remove(key : A, value : B) : Boolean = {
    val thunk = new OneShotThunk(value)
    under.remove(key, thunk)
  }
  
  def putIfAbsent(key : A, value : B) : Option[B] = {
    Option(under.putIfAbsent(key,value)).map {
      case b : B => b
      case OneShotThunk(v : B) => v
    }
  }
  
  def -=(key : A) : this.type = {
    under.remove(key)
    this
  }
  
  def +=(kv : (A,B)) : this.type = {
    val (key,value) = kv
    under.put(key,value)
    this
  }

  def get(key : A) : Option[B] = {
    Option(under.get(key)) match {
      case None => None
      case Some(OneShotThunk(v : B)) => Some(v)
      case Some(v : B) => Some(v)
    }
  }
  
  def iterator : Iterator[(A,B)] = {
    new Iterator[(A,B)] {
      val iter = under.entrySet.iterator
      
      def next : (A,B) = {
        val entry = iter.next
        val key = entry.getKey
        entry.getValue match {
          case t : OneShotThunk[_] => (key,t.value.asInstanceOf[B])
          case v => (key,v.asInstanceOf[B])
        }
      }
      
      def hasNext : Boolean = {
        iter.hasNext
      }
      
      def remove {
        iter.remove
      }
    }
  }
}