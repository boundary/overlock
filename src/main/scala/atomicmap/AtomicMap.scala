package atomicmap

import scala.collection.mutable.ConcurrentMap
import java.util.concurrent.{ConcurrentMap => JConcurrentMap, ConcurrentSkipListMap, ConcurrentHashMap}
import java.util.Comparator
import org.cliffc.high_scale_lib._

object AtomicMap {
  def atomicCSLM[A,B] : AtomicMap[A,B] = {
    new AtomicMap(new ConcurrentSkipListMap[A,OneShotThunk[B]])
  }
  
  def atomicCSLM[A,B](comp : Comparator[A]) : AtomicMap[A,B] = {
    new AtomicMap(new ConcurrentSkipListMap[A,OneShotThunk[B]](comp))
  }
  
  def atomicNBHM[A,B] : AtomicMap[A,B] = {
    new AtomicMap(new NonBlockingHashMap[A,OneShotThunk[B]])
  }
  
  def atomicNBHM[A,B](size : Int) : AtomicMap[A,B] = {
    new AtomicMap(new NonBlockingHashMap[A,OneShotThunk[B]](size))
  }
  
  def atomicCHM[A,B] : AtomicMap[A,B] = {
    new AtomicMap(new ConcurrentHashMap[A,OneShotThunk[B]])
  }
  
  def atomicCHM[A,B](cap : Int) : AtomicMap[A,B] = {
    new AtomicMap(new ConcurrentHashMap[A,OneShotThunk[B]](cap))
  }
  
  def atomicCHM[A,B](cap : Int, loadFactor : Float) : AtomicMap[A,B] = {
    new AtomicMap(new ConcurrentHashMap[A,OneShotThunk[B]](cap,loadFactor))
  }
  
  def atomicCHM[A,B](cap : Int, loadFactor : Float, concurrencyLevel : Int) : AtomicMap[A,B] = {
    new AtomicMap(new ConcurrentHashMap[A,OneShotThunk[B]](cap, loadFactor,concurrencyLevel))
  }
}

class AtomicMap[A,B](under : JConcurrentMap[A,OneShotThunk[B]]) extends ConcurrentMap[A,B] {

  override def getOrElseUpdate(key : A, op : => B) : B = {
    val t = new OneShotThunk(op)
    val thunk = under.putIfAbsent(key, t) match {
      case null => t
      case v => v
    }
    thunk.value
  }
  
  def replace(key : A, value : B) : Option[B] = {
    val thunk = new OneShotThunk(value)
    Option(under.replace(key,thunk)).map(_.value)
  }
  
  /**
   * This works because ConcurrentMap defines it as .equals
   */
  def replace(key : A, oldVal : B, newVal : B) : Boolean = {
    val oldThunk = new OneShotThunk(oldVal)
    val newThunk = new OneShotThunk(newVal)
    under.replace(key, oldThunk, newThunk)
  }
  
  def remove(key : A, value : B) : Boolean = {
    val thunk = new OneShotThunk(value)
    under.remove(key, thunk)
  }
  
  def putIfAbsent(key : A, value : B) : Option[B] = {
    val thunk = new OneShotThunk(value)
    Option(under.putIfAbsent(key,thunk)).map(_.value)
  }
  
  def -=(key : A) : this.type = {
    under.remove(key)
    this
  }
  
  def +=(kv : (A,B)) : this.type = {
    val (key,value) = kv
    val thunk = new OneShotThunk(value)
    under.put(key,thunk)
    this
  }

  def get(key : A) : Option[B] = {
    if (!under.containsKey(key)) {
      None
    } else {
      Some(under.get(key).value)
    }
  }
  
  def iterator : Iterator[(A,B)] = {
    new Iterator[(A,B)] {
      val iter = under.entrySet.iterator
      
      def next : (A,B) = {
        val entry = iter.next
        (entry.getKey,entry.getValue.value)
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