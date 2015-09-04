import com.typesafe.sbt.SbtScalariform._
import sbt.Keys._
import scalariform.formatter.preferences._




name          := "reactive-ticket-monster"
organization  := "de.holisticon.example"
version       := "0.1"
scalaVersion  := "2.11.7"

// docker image configurationellei
maintainer in Docker := "Daniel Wegener (Holisticon AG)"
dockerExposedPorts := List(8080)
dockerBaseImage := "java:8-jre"

unmanagedSourceDirectories in Compile <<= (scalaSource in Compile)( _ :: Nil)

libraryDependencies ++= {
	val akkaV = "2.4.0-RC1"
	val akkaStreamV = "1.0"

	Seq(
		"com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamV,
		"com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamV,
		"com.typesafe.akka" %% "akka-http-core-experimental" % akkaStreamV,
		"com.typesafe.akka" %% "akka-http-experimental" % akkaStreamV,
		"com.typesafe.akka" %% "akka-stream-testkit-experimental" % akkaStreamV % "test",
		"com.typesafe.akka" %% "akka-actor" % akkaV,
		"com.typesafe.akka" %% "akka-remote" % akkaV,
		"com.typesafe.akka" %% "akka-cluster" % akkaV,
		"com.typesafe.akka" %% "akka-cluster-sharding" % akkaV,
		"com.typesafe.akka" %% "akka-cluster-metrics" % akkaV,
		"com.typesafe.akka" %% "akka-slf4j" % akkaV,
		"com.typesafe.akka" %% "akka-distributed-data-experimental" % akkaV,
		"com.typesafe.akka" %% "akka-persistence-query-experimental" % akkaV,
		"com.typesafe.akka" %% "akka-persistence" % akkaV,
		"de.heikoseeberger" %% "akka-sse" % "1.1.0",
		"ch.qos.logback" % "logback-classic" % "1.1.3",
		"com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
		"org.specs2" %% "specs2" % "2.4.2" % "test",
		"org.scala-lang" % "scala-compiler" % scalaVersion.value,
		"io.kamon" % "sigar-loader" % "1.6.6-rev002",
		"org.slf4j" % "log4j-over-slf4j" % "1.7.12" % "runtime",

		// WebJars
		"org.webjars.bower" % "angular" % "1.4.5",
		"org.webjars.bower" % "angular-route" % "1.4.5",
		"org.webjars.bower" % "angular-resource" % "1.4.5",
		"org.webjars.bower" % "epoch" % "0.6.0",
		"org.webjars.bower" % "d3" % "3.5.6",
		"org.webjars.bower" % "backbone" % "1.1.2" force(), //note :otherwise it pulls up underscore
		"org.webjars.bower" % "bootstrap" % "3.3.5" exclude("org.webjars.bower", "jquery"),
		"org.webjars.bower" % "jquery" % "2.1.4",
		"org.webjars.bower" % "requirejs" % "2.1.20",
		"org.webjars.bower" % "underscore" % "1.6.0" force(), //note: bumping this a minor higher breaks the ui. great stuff.
		"org.webjars.bower" % "requirejs-text" % "2.0.14",
		"org.webjars" % "font-awesome" % "4.4.0",

		// Gatling stress tests
		"io.gatling.highcharts" % "gatling-charts-highcharts" % "2.1.7" % "test",
		"io.gatling" % "gatling-test-framework" % "2.1.7" % "test",
		"ch.qos.logback" % "logback-classic" % "1.1.3")
}



scalacOptions ++= List(
	"-unchecked",
	"-deprecation",
	"-language:_",
	"-target:jvm-1.8",
	"-encoding", "UTF-8",
	"-Xlint",
	"-Xfatal-warnings"
)

publishMavenStyle := true

javacOptions ++= List(
  "-source", "1.8",
  "-target", "1.8"
)

scalariformSettings

Revolver.settings

enablePlugins(SbtNativePackager)
enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
enablePlugins(GatlingPlugin)

// required for libraries
fork := true

