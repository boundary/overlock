Overlock
============

Overlock is a concurrency utility library for Scala.  It is currently comprised of three modules: atomicmap, lock, and threadpool.  

To add overlock as a dependency to a maven project, add the following dependency and repo definitions.

      <dependency>
         <groupId>com.boundary</groupId>
         <artifactId>overlock-scala_${scala.version}</artifactId>
         <version>0.6.0</version>
      </dependency>

      <repository>
         <id>BoundaryPublicRepo</id>
         <name>Boundary Public Repo</name>
         <url>http://maven.boundary.com/artifactory/repo/</url>
      </repository>

To add overlock as a dependency to an sbt project put the following in your project file:

      val boundaryPublic = "Boundary Public Repo" at "http://maven.boundary.com/artifactory/repo"
      
      val overlock = "com.boundary" %% "overlock" % "0.6.0"
      
Overlock is currently supported on Scala 2.9.1.

AtomicMap
--------

The AtomicMap trait provides atomic behavior for the `getOrElseUpdate` method.  It does so via a removal memoizer pattern, explained [here](http://blog.boundary.com/2011/05/03/atomicmap-solutions.html).  The AtomicMap object has factory methods for creating AtomicMap instances backed by [ConcurrentHashMap](http://download.oracle.com/javase/6/docs/api/java/util/concurrent/ConcurrentHashMap.html), [ConcurrentSkipListMap](http://download.oracle.com/javase/6/docs/api/java/util/concurrent/ConcurrentSkipListMap.html) or [NonBlockingHashMap](https://github.com/boundary/high-scale-lib/blob/master/src/org/cliffc/high_scale_lib/NonBlockingHashMap.java).  Use like so:

    import overlock.atomicmap._
    
    //ConcurrentSkipListMap
    val cslm = AtomicMap.atomicCSLM[String,Any](new CustomComparator)
    
    //ConcurrentHashMap
    val chm = AtomicMap.atomicCHM[String,Any](10000, 0.75, 32)
    
    //NonBlockingHashMap
    val nbhm = AtomicMap.atomicNBHM[String,Any](10000)
    
Threadpool
---------

The threadpool module provides [Executor](http://download.oracle.com/javase/6/docs/api/java/util/concurrent/Executor.html) instances backed by thread pools.  ThreadPool is a factory object with methods for creating a few different kinds of Executors.  All of the executors created by ThreadPool are instrumented using Coda Hale's excellent [metrics](https://github.com/codahale/metrics) project.  The instrumentation tracks the rate at which work is inserted, the rate of work rejection, summary statistics of work latency, and gauge readings on work queue size, thread pool size, and active thread count.

    import overlock.threadpool._

    val cachedPool = ThreadPool.instrumentedCached("com.myapplication", "RequestPool1")
    
The instrumented cached thread pool has the same thread creation behavior as [Executors.newCachedThreadPool()](http://download.oracle.com/javase/6/docs/api/java/util/concurrent/Executors.html#newCachedThreadPool%28%29).  The `path` and `name` parameters control the JMX path and name respectively.

    val fixedPool = ThreadPool.instrumentedFixed("com.myapplication", "RequestPool2", 16)
    
The instrumented fixed thread pool has the same thread creation behavior as [Executors.newFixedThreadPool(int)](http://download.oracle.com/javase/6/docs/api/java/util/concurrent/Executors.html#newFixedThreadPool%28int%29).  `n` controls the fixed thread pool size and `path` and `name` are as above.

    val elasticPool = ThreadPool.instrumentedElastic("com.myapplication", "RequestPool3", 10, 50)
    
The instrumented elastic thread pool has an entirely different thread creation behavior from any of the thread pools available in the java.util.concurrent package.  The elastic thread pool will favor the creation of new threads up to the given maximum number of threads.  After the thread limit has been reached the elastic thread pool will queue incoming work unboundedly.  This is in contrast to normal [ThreadPoolExecutor](http://download.oracle.com/javase/6/docs/api/java/util/concurrent/ThreadPoolExecutor.html) behavior, which will favor queueing over thread creation.

Lock
-------

The lock module contains two locking utilities, Lock and SpinLock.  Lock is a wrapper for [ReentrantReadWriteLock](http://download.oracle.com/javase/6/docs/api/java/util/concurrent/locks/ReentrantReadWriteLock.html).  It provides idiomatic Scala syntax wrappers for locking:

    import overlock.lock._

    val lock = new Lock
    
    lock.readLock {
      //do work that can be shared by many reader threads
    }
    
    lock.writeLock {
      //do work that requires exclusive access by one thread
    }
    
Spinlock is a utility to provide a form of fast locking known as spinlocks.  [Spinlocks](http://en.wikipedia.org/wiki/Spinlock) prefer to repeatedly check that a lock is released rather than let the JVM put the thread to sleep.  Spinlocks are useful for increasing throughput when it is known in advance that the critical section will be locked for less time than it takes to perform a context switch between threads.  Spinlocks, however, will provide no benefit on single core machines and may reduce overall throughput.

    import overlock.lock._
    
    val lock = new SpinLock
    
    lock.readLock {
      //do work that can be shared by many reader threads
    }
    
    lock.writeLock {
      //do work that requires exclusive access by one thread
    }
    
