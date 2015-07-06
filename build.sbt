import com.typesafe.sbt.SbtNativePackager

name := "test-native-packager"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += "spray repo" at "http://repo.spray.io"

herokuAppName in Compile := "pacific-meadow-xxxx"

libraryDependencies ++= {
  val identityV = "3.44"
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  val sprayJsonV = "1.3.1"
  Seq(
    "com.gu.identity"     %%  "identity-crypto" % identityV,
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray"            %%  "spray-client"  % sprayV,
    "io.spray"            %%  "spray-json"    % sprayJsonV,
    "io.spray"            %%  "spray-testkit" % sprayV   % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-slf4j"    % akkaV
  )
}

mainClass in Compile := Some("info.lindblad.pedanticpadlock.bootstrap.Main")

lazy val root = (project in file("."))
  .enablePlugins(SbtNativePackager)
  .enablePlugins(JavaServerAppPackaging)