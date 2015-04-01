val scalaVsn = "2.11.6"

resolvers += "Boundary Public Repo" at "http://maven.boundary.com/artifactory/repo"

libraryDependencies ++= Seq(
  "com.yammer.metrics" % "metrics-core" % "2.2.0",
  "com.yammer.metrics" % "metrics-scala_2.9.1" % "2.2.0",
  "com.boundary" % "high-scale-lib" % "1.0.5",
  "org.scala-tools.testing" % "specs_2.9.1" % "1.6.9",
  "junit" % "junit" % "4.8.2" % "test",
  "org.slf4j" % "slf4j-api" % "1.7.6",
  "org.slf4j" % "slf4j-simple" % "1.7.6" % "test"
)

lazy val overlock = (project in file(".")).
  settings(
    name := "overlock",
    scalaVersion := scalaVsn
  )
