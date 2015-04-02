val scalaVsn = "2.11.6"

resolvers += "Boundary Public Repo" at "http://maven.boundary.com/artifactory/repo"

libraryDependencies ++= Seq(
  "nl.grons" %% "metrics-scala" % "3.4.0",
  "com.boundary" % "high-scale-lib" % "1.0.5",
  "org.slf4j" % "slf4j-api" % "1.7.6",

  "org.specs2" %% "specs2" % "2.4.15" % "test",
  "junit" % "junit" % "4.8.2" % "test",
  "org.slf4j" % "slf4j-simple" % "1.7.6" % "test"
)

lazy val overlock = (project in file(".")).
  settings(
    name := "overlock",
    scalaVersion := scalaVsn
  )
