import com.typesafe.sbt.SbtNativePackager

name := "pedantic-padlock"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += "spray repo" at "http://repo.spray.io"

herokuAppName in Compile := "pedantic-padlock"

libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  val sprayJsonV = "1.3.1"
  Seq(
    "io.spray"            %%  "spray-can"       % sprayV,
    "io.spray"            %%  "spray-routing"   % sprayV,
    "io.spray"            %%  "spray-client"    % sprayV,
    "io.spray"            %%  "spray-caching"   % sprayV,
    "com.typesafe.akka"   %%  "akka-actor"      % akkaV,
    "com.typesafe.akka"   %%  "akka-slf4j"      % akkaV,
    "net.debasishg"       %%  "redisclient"     % "3.0",
    "ch.qos.logback"      %   "logback-classic" % "1.1.2",
    "org.json4s"          %% "json4s-native"    % "3.2.10",
    "commons-validator"   % "commons-validator" % "1.4.0",
    "io.spray"            %%  "spray-testkit"   % sprayV   % "test",
    "org.scalatest"       % "scalatest_2.11"    % "2.2.4"  % "test",
    "com.orange.redis-embedded" % "embedded-redis" % "0.6" % "test"
  )
}

mainClass in Compile := Some("info.lindblad.pedanticpadlock.bootstrap.Main")

lazy val root = (project in file("."))
  .enablePlugins(SbtNativePackager)
  .enablePlugins(JavaServerAppPackaging)
