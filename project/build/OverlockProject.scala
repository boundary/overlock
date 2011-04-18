import sbt._
import sbt.StringUtilities._

class OverlockProject(info : ProjectInfo) extends DefaultProject(info) {
  val codaRepo = "Coda Hale's Biznazz" at "http://repo.codahale.com/"
  val boundary = "Boundary Public Repo" at "http://maven.boundary.com/artifactory/repo"
  
  val specs = "org.scala-tools.testing" %% "specs" % "1.6.7" % "test"
  val metrics = "com.yammer" %% "metrics" % "2.0.0-BETA10"
  val highScale = "com.boundary" % "high-scale-lib" % "1.0.1"
}