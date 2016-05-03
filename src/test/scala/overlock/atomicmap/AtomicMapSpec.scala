package overlock.atomicmap

import org.specs2.mutable._
import scala.collection.concurrent.{Map => ScalaConcurrentMap}
import java.util.concurrent.atomic._


abstract class AtomicMapSpec extends Specification {
  isolated

  def createMap[A,B] : ScalaConcurrentMap[A,B]

  "AtomicMap" >> {
    val map = createMap[String,Int]

    "be a full concurrent.Map implementation" >> {
      "getOrElseUpdate" >> {
        map.getOrElseUpdate("blah", 1) mustEqual 1
        map.getOrElseUpdate("blah", 2) mustEqual 1
      }

      "replace(k,v)" >> {
        map.replace("blah", 1) must beNone
        map.put("blah", 1)
        map.replace("blah", 2) must beSome(1)
      }

      "replace(k, o, n)" >> {
        map.replace("blah", 2, 1) must beFalse
        map.put("blah", 1)
        println("map " + map)
        map.replace("blah", 1, 2) must beTrue
        map("blah") mustEqual 2
      }

      "remove" >> {
        map.put("blah", 1)
        map.remove("blah", 2) must beFalse
        map.remove("blah", 1) must beTrue
        map.get("blah") must beNone
      }

      "putIfAbsent" >> {
        map.putIfAbsent("blah", 1) must beNone
        map.putIfAbsent("blah", 2) must beSome(1)
        map.putIfAbsent("derp", 3) must beNone
      }

      "-=" >> {
        map.put("herp", 1)
        map -= "herp"
        map.get("herp") must beNone
      }

      "+=" >> {
        map += (("herp", 1))
        map.get("herp") must beSome(1)
      }

      "iterator" >> {
        map.put("herp", 1)
        map.put("derp", 2)

        val otherMap = createMap[String,Int]

        for ((key,value) <- map) {
          otherMap.put(key,value)
        }

        otherMap.get("herp") must beSome(1)
        otherMap.get("derp") must beSome(2)
      }

      "get" >> {
        map.put("herp", 5)
        map.get("herp") must beSome(5)
      }

      "return a new empty" >> {
        map.put("herp", 1)
        val emptyMap = map.empty
        emptyMap.get("herp") must beNone
      }
    }

    "evaluate op only once" >> {
      val counter = new AtomicInteger(0)

      val threads = for (i <- (0 to 5)) yield {
        new Thread {
          override def run {
            map.getOrElseUpdate("blah", {Thread.sleep(100); counter.incrementAndGet})
          }
        }
      }
      threads.foreach(_.start)
      threads.foreach(_.join)
      map("blah") mustEqual 1
      counter.get mustEqual 1
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
