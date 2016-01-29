import com.typesafe.sbt.SbtScalariform._
import sbt.Keys._



lazy val commonSettings = Seq(
	organization := "de.holisticon.showcase",
	organizationName := "Holisticon AG",
	organizationHomepage := Some(url("https://www.holisticon.de")),
	version := "0.1",
	scmInfo := Some(ScmInfo(url("https://github.com/holisticon/reactive-ticket-booking"),
		"git@github.com:holisticon/reactive-ticket-booking.git")),

	// docker image configuration
	maintainer in Docker := "Daniel Wegener (Holisticon AG)",
	maintainer := "Daniel Wegener (Holisticon AG)",
	scalaVersion := "2.11.7",
	javacOptions ++= List(
	"-source", "1.8",
	"-target", "1.8"
	),
	scalacOptions in Global ++= List(
	"-unchecked",
	"-deprecation",
	"-language:_",
	"-target:jvm-1.8",
	"-encoding", "UTF-8",
	"-Xlint",
	"-Xfatal-warnings"
	)
)

lazy val akkaV = "2.4.2-RC1"

lazy val `reactive-ticket-booking-parent` =
	(project in file(".")).aggregate(app, web, stress)
	.settings(
			run in Compile <<= run in Compile in app
		)


lazy val web = project
	.enablePlugins(SbtWeb)
	.settings(commonSettings : _*)
	.settings(
		name := "reactive-ticket-booking-web",
		libraryDependencies := Seq(
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
	"org.webjars.bower" % "lazysizes" % "1.2.0",
	"org.webjars" % "font-awesome" % "4.4.0"
	),
		autoScalaLibrary := false,
		// sbt-web
		//includeFilter in (Assets, gzip) := "*.html" || "*.css" || "*.js" || "*.svg",
		includeFilter in(Assets, LessKeys.less) := "*.less",
		LessKeys.compress in Assets := true

		//(managedResources in Runtime) += (packageBin in Assets).value,

		//WebKeys.packagePrefix in Assets := "public/",

	)

lazy val app = project
	.dependsOn(web)
	.settings(name := "reactive-ticket-booking")
	.settings(commonSettings : _*)
	.enablePlugins(SbtNativePackager)
	.enablePlugins(JavaAppPackaging)
	.enablePlugins(DockerPlugin)
	.enablePlugins(UniversalDeployPlugin)
	.settings(



		libraryDependencies ++= Seq(
			"com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
			"com.typesafe.akka" %% "akka-stream" % akkaV,
			"com.typesafe.akka" %% "akka-http-core" % akkaV,
			"com.typesafe.akka" %% "akka-http-experimental" % akkaV,
			"com.typesafe.akka" %% "akka-stream-testkit" % akkaV % "test",
			"com.typesafe.akka" %% "akka-actor" % akkaV,
			"com.typesafe.akka" %% "akka-remote" % akkaV,
			"com.typesafe.akka" %% "akka-cluster" % akkaV,
			"com.typesafe.akka" %% "akka-cluster-sharding" % akkaV,
			"com.typesafe.akka" %% "akka-cluster-metrics" % akkaV,
			"com.typesafe.akka" %% "akka-slf4j" % akkaV,
			"com.typesafe.akka" %% "akka-distributed-data-experimental" % akkaV,
			"com.typesafe.akka" %% "akka-persistence-query-experimental" % akkaV,
			"com.typesafe.akka" %% "akka-persistence" % akkaV,
			"de.heikoseeberger" %% "akka-sse" % "1.6.1",
			"ch.qos.logback" % "logback-classic" % "1.1.3",
			"com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
			"org.specs2" %% "specs2" % "2.4.2" % "test",
			"org.scala-lang" % "scala-compiler" % scalaVersion.value,
			"io.kamon" % "sigar-loader" % "1.6.6-rev002",
			"org.slf4j" % "log4j-over-slf4j" % "1.7.12" % "runtime"),


		packageName in Docker := "holisticon/" + packageName.value,
		version in Docker := version.value,

		dockerExposedPorts := List(8080, 2551),
		dockerBaseImage := "java:8-jre",
		dockerUpdateLatest := true,
		dockerRepository := Some("danielwegener-docker-registry.bintray.io"),

		publishMavenStyle := true,

		scalariformSettings,

		Revolver.settings,
		// required for libraries
		connectInput in run := true
	)

lazy val stress = project
	.enablePlugins(GatlingPlugin)
	.settings(commonSettings : _*)
	.settings(
		libraryDependencies ++= Seq(
			"io.gatling.highcharts" % "gatling-charts-highcharts" % "2.1.7" % "test",
			"io.gatling" % "gatling-test-framework" % "2.1.7" % "test",
			"ch.qos.logback" % "logback-classic" % "1.1.3")
	)

addCommandAlias("node1", "~reStart -Dbooking.netty.port=2551 -Dbooking.http.port=8080")
addCommandAlias("node2", "~reStart -Dbooking.netty.port=2552 -Dbooking.http.port=8081")
addCommandAlias("node3", "~reStart -Dbooking.netty.port=2553 -Dbooking.http.port=8082")

