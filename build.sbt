
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

lazy val akkaV = "2.4.2"

/*lazy val root = (project in file("."))
	.settings(name := "reactive-ticket-booking")
	.aggregate(app, stress)
	.settings(
			run in Compile <<= run in Compile in app,


	)
	*/

lazy val `reactive-ticket-booking` = (project in file("."))
	.settings(commonSettings : _*)
	.enablePlugins(SbtNativePackager)
	.enablePlugins(JavaAppPackaging)
	.enablePlugins(DockerPlugin)
	.enablePlugins(UniversalDeployPlugin)
	.settings(
		name := "reactive-ticket-booking",
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
			"de.heikoseeberger" %% "akka-sse" % "1.6.3",
			"ch.qos.logback" % "logback-classic" % "1.1.5",
			"com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
			"org.specs2" %% "specs2-core" % "3.7.1" % "test",
			"io.kamon" % "sigar-loader" % "1.6.6-rev002",
			"org.slf4j" % "log4j-over-slf4j" % "1.7.16" % "runtime",
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
			"org.webjars" % "font-awesome" % "4.5.0",
			"io.gatling.highcharts" % "gatling-charts-highcharts" % "2.1.7" % "test",
			"io.gatling" % "gatling-test-framework" % "2.1.7" % "test"
		),

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
		connectInput in run := true,

		addCommandAlias("node1", "~reStart -Dbooking.netty.port=2551 -Dbooking.http.port=8080"),
		addCommandAlias("node2", "~reStart -Dbooking.netty.port=2552 -Dbooking.http.port=8081"),
		addCommandAlias("node3", "~reStart -Dbooking.netty.port=2553 -Dbooking.http.port=8082")
	)

lazy val stress = (project in file("stress"))
	.settings(commonSettings : _*)
	.enablePlugins(GatlingPlugin)
	.settings(
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
		),
		libraryDependencies ++= Seq(
			"io.gatling.highcharts" % "gatling-charts-highcharts" % "2.1.7" % "test",
			"io.gatling" % "gatling-test-framework" % "2.1.7" % "test",
			"ch.qos.logback" % "logback-classic" % "1.1.3")
	)


fork in run := true
