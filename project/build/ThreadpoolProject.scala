import sbt._
import sbt.StringUtilities._

class ThreadpoolProject(info : ProjectInfo) extends DefaultProject(info) {
  val codaRepo = "Coda Hale's Biznazz" at "http://repo.codahale.com/"
  
  val specs = "org.scala-tools.testing" %% "specs" % "1.6.7" % "test"
  val metrics = "com.yammer" %% "metrics" % "2.0.0-BETA10"
  
}