name := "overlock-scala"

organization := "com.gilt"

scalaVersion := "2.11.8"

publishTo := {
  val nexus = "https://nexus.gilt.com/nexus/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/gilt.snapshots")
  else
    Some("releases"  at nexus + "content/repositories/internal-releases/")
}

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

libraryDependencies ++= Seq(
    "nl.grons"                  %%   "metrics-scala"   % "3.5.2",
    "com.boundary"               %   "high-scale-lib"  % "1.0.6",
    "org.specs2"                %%   "specs2-core"     % "3.7.2"    % "test",
    "org.scala-lang.modules"    %%   "scala-xml"       % "1.0.5"    % "test"
)
