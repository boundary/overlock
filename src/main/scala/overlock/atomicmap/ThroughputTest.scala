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
  val (warmupDuration,warmupOperations) = run(1, warmupTime)
  println("Warmup took " + warmupDuration / 1000000000.0 + "s for " + warmupOperations + " ops.")
  
  def run {
    println("noThreads\t" + (1 to noTrials).map("trial_" + _).mkString("\t") + "\tmean\tstddv")
    for (numThreads <- Range(threadMin, threadMax+1, threadInc)) {
      print(numThreads + "\t")
      val throughputs = for (trial <- (1 to noTrials)) yield {
        val (duration,operations) = run(numThreads, trialTime)
        val throughput = operations / (duration / 1000000000.0)
        print(throughput + "\t")
        throughput
      }
      val mean = throughputs.reduceLeft(_ + _) / throughputs.size
      val variance = throughputs.map({ n => pow(n - mean, 2) }).reduceLeft(_ + _) / throughputs.size
      val stddev = sqrt(variance)
      println(mean + "\t" + stddev)
    }
  }
  
  protected def run(numThreads : Int, timeout : Long) : (Long,Long) = {
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
    (duration,operations)
  }
}

class ThroughputThread(map : ConcurrentMap[String,String], keys : Array[String], startBarrier : CyclicBarrier) extends Thread {
  val random = new Random
  val operations = new AtomicLong(0)
  override def run {
    startBarrier.await
    while (! Thread.interrupted) {
      doOperation
    }
  }
  
  protected def doOperation {
    val key = keys(random.nextInt(keys.length))
    map.getOrElseUpdate(key, key)
    operations.getAndIncrement
  }
}