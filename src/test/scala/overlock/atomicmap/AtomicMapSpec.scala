package overlock.atomicmap

import java.util.concurrent.atomic._

import scala.collection.concurrent.Map

import org.specs2.mutable._

abstract class AtomicMapSpec extends SpecificationWithJUnit {
  def createMap[A,B] : Map[A,B]
  
  "AtomicMap" should {
    val map = createMap[String,Int]
    
    "be a full concurrentmap implementation" in {
      "getOrElseUpdate" in {
        map.getOrElseUpdate("getOrElseUpdate", 1) must beEqualTo(1)
        map.getOrElseUpdate("getOrElseUpdate", 2) must beEqualTo(1)
      }

      "replace(k,v)" in {
        map.replace("replace(k,v)", 1) must beNone
        map.put("replace(k,v)", 1)
        map.replace("replace(k,v)", 2) must beSome(1)
      }

      "replace(k, o, n)" in {
        map.replace("replace(k, o, n)", 2, 1) must beFalse
        map.put("replace(k, o, n)", 1)
        map.replace("replace(k, o, n)", 1, 2) must beTrue
        map("replace(k, o, n)") must beEqualTo(2)
      }

      "remove" in {
        map.put("remove", 1)
        map.remove("remove", 2) must beFalse
        map.remove("remove", 1) must beTrue
        map.get("remove") must beNone
      }

      "putIfAbsent" in {
        map.putIfAbsent("putIfAbsent-first", 1) must beNone
        map.putIfAbsent("putIfAbsent-first", 2) must beSome(1)
        map.putIfAbsent("putIfAbsent-second", 3) must beNone
      }

      "-=" in {
        map.put("-=", 1)
        map -= "-="
        map.get("-=") must beNone
      }

      "+=" in {
        map += "+=" -> 1
        map.get("+=") must beSome(1)
      }

      "iterator" in {
        map.put("iterator-first", 1)
        map.put("iterator-second", 2)
        
        val otherMap = createMap[String,Int]

        for ((key,value) <- map) {
          otherMap.put(key,value)
        }
        
        otherMap.get("iterator-first") must beSome(1)
        otherMap.get("iterator-second") must beSome(2)
      }
      
      "get" in {
        map.put("get", 5)
        map.get("get") must beSome(5)
      }
      
      "return a new empty" in {
        map.put("return a new empty", 1)
        val emptyMap = map.empty
        emptyMap.get("return a new empty") must beNone
      }
    }
    
    "evaluate op only once" in {
      val counter = new AtomicInteger(0)
      
      val threads = for (i <- (0 to 5)) yield {
        new Thread {
          override def run {
            map.getOrElseUpdate("evaluate op only once", {Thread.sleep(100); counter.incrementAndGet})
          }
        }
      }
      threads.foreach(_.start)
      threads.foreach(_.join)
      map("evaluate op only once") must beEqualTo(1)
      counter.get must beEqualTo(1)
    }
  }
}

class AtomicNBHMSpec extends AtomicMapSpec {
  def createMap[A,B] = AtomicMap.atomicNBHM
}

class AtomicCSLMSpec extends AtomicMapSpec {
  def createMap[A,B] = AtomicMap.atomicCSLM
}

class AtomicCHMSpec extends AtomicMapSpec {
  def createMap[A,B] = AtomicMap.atomicCHM
}
