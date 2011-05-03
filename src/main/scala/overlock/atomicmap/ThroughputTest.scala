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
import java.util.{concurrent => juc}
import juc.atomic._
import juc.CyclicBarrier
import java.util.Random
import scala.math._

object ThroughputTest {
  
  def main(args : Array[String]) {
    val test = new ThroughputTest {
      override def createMap[A,B](size : Int) = AtomicMap.atomicNBHM(size)
    }
    
    test.run
  }
}

abstract class ThroughputTest {
  def createMap[A,B](size : Int) : ConcurrentMap[A,B]
  
  val threadMin = 1
  val threadMax = 8
  val threadInc = 1
  val tableSize = 1000000
  val trialTime = 5000
  val noTrials = 20
  val warmupTime = 30000
  
  var keymax = 1
  while (keymax < tableSize) { keymax <<= 1 }
  
  val keys = new Array[String](keymax)
  for(i <- (0 until keymax)) {
    keys(i) = i + "abc" + (i*17+123)
  }
  println(Runtime.getRuntime.availableProcessors + " CPU's")
  println("created " + keymax + " keys")
  println("warming up JIT")
  //warmup
  val (warmupDuration,warmupOperations,warmupWrites) = run(1, warmupTime)
  println("Warmup took " + warmupDuration / 1000000000.0 + "s for " + warmupOperations + " ops with ratio " + (warmupWrites.toDouble/warmupOperations))
  
  def run {
    println("noThreads\t" + (1 to noTrials).map("trial_" + _).mkString("\t") + "\tmean\tstddv")
    for (numThreads <- Range(threadMin, threadMax+1, threadInc)) {
      print(numThreads + "\t")
      val (throughputs,ratios) = (for (trial <- (1 to noTrials)) yield {
        val (duration,operations,writes) = run(numThreads, trialTime)
        val throughput = operations / (duration / 1000000000.0)
        print(throughput + "\t")
        (throughput, writes.toDouble / operations)
      }).unzip
      printLineStats(throughputs, false)
      printLineStats(ratios, true)
    }
  }
  
  protected def printLineStats(stats : Seq[Double], printPreamble : Boolean) {
    val preamble = stats.mkString("\t")
    val mean = stats.reduceLeft(_ + _) / stats.size
    val variance = stats.map({ n => pow(n - mean, 2) }).reduceLeft(_ + _) / stats.size
    val stddev = sqrt(variance)
    if (printPreamble) {
      println(preamble + "\t" + mean + "\t" + stddev)
    } else {
      println(mean + "\t" + stddev)
    }
    
  }
  
  protected def run(numThreads : Int, timeout : Long) : (Long,Long, Long) = {
    val map = createMap[String,String](tableSize)
    @volatile var startTime : Long = 0
    val barrier = new CyclicBarrier(numThreads, new Runnable {
      def run {
        startTime = System.nanoTime
      }
    })
    val threads = for (i <- (0 until numThreads)) yield {
      new ThroughputThread(map, keys, barrier)
    }

    threads.foreach(_.start)
    Thread.sleep(timeout)
    threads.foreach(_.interrupt)
    threads.foreach(_.join)
    val endTime = System.nanoTime
    val duration = endTime - startTime
    val operations = threads.map(_.operations.get).reduceLeft(_ + _)
    val writes = threads.map(_.writes.get).reduceLeft(_ + _)
    (duration,operations, writes)
  }
}

class ThroughputThread(map : ConcurrentMap[String,String], keys : Array[String], startBarrier : CyclicBarrier) extends Thread {
  val random = new Random()
  val operations = new AtomicLong(0)
  val writes = new AtomicLong(0)
  override def run {
    startBarrier.await
    while (! Thread.interrupted) {
      doOperation
    }
  }
  
  protected def doOperation {
    val i = random.nextInt(keys.length)
/*    println(i)*/
    val key = keys(i)
    map.getOrElseUpdate(key, {writes.getAndIncrement; key})
    operations.getAndIncrement
  }
}