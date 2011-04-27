package overlock.atomicmap

import java.util.concurrent.{ConcurrentNavigableMap, ConcurrentSkipListMap}
import java.util.{NavigableMap, NavigableSet}

class AtomicNavigableMap[A,B](u : => ConcurrentNavigableMap[A,Any]) extends AtomicMap[A,B](u) {

  override lazy val under = u
  def comparator = under.comparator
  
  override def empty = new AtomicNavigableMap[A,B](u)
  
  def subMap(fromKey : A, fromInclusive : Boolean, toKey : A, toInclusive : Boolean) : AtomicNavigableMap[A,B] = {
    new AtomicNavigableMap(under.subMap(fromKey,fromInclusive,toKey,toInclusive))
  }
}
