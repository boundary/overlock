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
  lazy val under = u

  override def empty = new AtomicMap[A,B](u)
  
  override def getOrElseUpdate(key : A, op : => B) : B = {
    val t = new OneShotThunk(op)
    under.putIfAbsent(key, t) match {
      case null => 
        val ve = t.value
        if (under.replace(key, t, ve)) {
          ve
        } else { //will only happen if client code is mixing getOrElseUpdate and put
          get(key) match {
            case Some(thunk : OneShotThunk[_]) => thunk.value.asInstanceOf[B]
            case Some(v : B) => v
            case None => //loop around to the beginning
              getOrElseUpdate(key, ve)
          }
        }
      case thunk : OneShotThunk[_] => thunk.value.asInstanceOf[B]
      case v : B => v
    }
  }
  
  def replace(key : A, value : B) : Option[B] = {
    Option(under.replace(key,value)).map(_.asInstanceOf[B])
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
    Option(under.putIfAbsent(key,value)).map(_.asInstanceOf[B])
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
      case Some(t : OneShotThunk[_]) => Some(t.value.asInstanceOf[B])
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