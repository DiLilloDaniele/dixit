name := "dixit"

version := "0.1"

scalaVersion := "3.2.0"

mainClass := Some("MainClass")
Compile / run / mainClass := Some("MainClass")

lazy val akkaVersion = "2.7.0"
lazy val akkaGroup = "com.typesafe.akka"

lazy val startupTransition: State => State = "conventionalCommits" :: _

coverageEnabled := true

lazy val root = project
  .in(file("."))
  .settings(
    // Other settings...
    Global / onLoad := {
      val old = (Global / onLoad).value
      startupTransition compose old
    }
  )

libraryDependencies ++= Seq(
  akkaGroup %% "akka-actor-typed" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "mysql" % "mysql-connector-java" % "5.1.44",
  akkaGroup %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.2.11" % Test,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion, // For akka remote
  "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion, // akka clustering module
  "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion
)

enablePlugins(AkkaGrpcPlugin)