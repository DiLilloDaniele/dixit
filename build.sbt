name := "dixit"

version := "0.1"

scalaVersion := "3.2.0"
//sbt "runMain grpcService.client.actors.ManualTesting"
mainClass := Some("grpcService.client.view.HomepageView")
Compile / run / mainClass := Some("grpcService.client.view.HomepageView")

lazy val akkaVersion = "2.7.0"
lazy val akkaGroup = "com.typesafe.akka"

lazy val startupTransition: State => State = "conventionalCommits" :: _

coverageEnabled := true

coverageExcludedPackages := "<empty>;.*view.*;view;.view.*;.view.;view.*;target/.*;"

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
  "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
  "org.testcontainers" % "testcontainers" % "1.19.1",
  "org.testcontainers" % "mysql" % "1.19.1"
)

val runApp = taskKey[Unit]("sbt equivalent of gradle's JavaExec")
runApp := {
  (runner in Compile).value.run(
    mainClass = "grpcService.client.actors.utils.ManualTesting",
    classpath = (fullClasspath in Runtime).value.files,
    options = Array(""),
    log = streams.value.log
  )
}

val runPrioExample = taskKey[Unit]("running prio queue mailbox example")
runPrioExample := {
  (runner in Compile).value.run(
    mainClass = "grpcService.client.actors.prioritizedQueueExample.PriorityMailBoxApp",
    classpath = (fullClasspath in Runtime).value.files,
    options = Array(""),
    log = streams.value.log
  )
}
val runNewPlayer = taskKey[Unit]("sbt equivalent of gradle's JavaExec")
runNewPlayer := {
  (runner in Compile).value.run(
    mainClass = "grpcService.client.actors.utils.StartNewPlayer",
    classpath = (fullClasspath in Runtime).value.files,
    options = Array(""),
    log = streams.value.log
  )
}

val runServer = taskKey[Unit]("sbt equivalent of gradle's JavaExec")
runServer := {
  (runner in Compile).value.run(
    mainClass = "grpcService.server.applicationService.Service",
    classpath = (fullClasspath in Runtime).value.files,
    options = Array(""),
    log = streams.value.log
  )
}

val dbManualTest = taskKey[Unit]("sbt equivalent of gradle's JavaExec")
dbManualTest := {
  (runner in Compile).value.run(
    mainClass = "grpcService.server.data.adapters.AccessAdapter",
    classpath = (fullClasspath in Runtime).value.files,
    options = Array(""),
    log = streams.value.log
  )
}

enablePlugins(AkkaGrpcPlugin)

