import Dependencies._
import sbt.enablePlugins


lazy val commonSettings = Seq(
  organization    := "com.ivanwakeup",
  scalaVersion    := "2.12.8"
)

lazy val root = Project(
  id = "crawler",
  base = file(".")
).settings(commonSettings).aggregate(core, web)

lazy val core = Project(
  id = "core",
  base = file("core")
).
  settings(commonSettings,
    name := "core",
    resolvers += Resolver.mavenLocal,
    resolvers += Resolver.bintrayRepo("hseeberger", "maven")
).settings(libraryDependencies ++= coreDeps)


lazy val webDockerSettings = Seq(
  packageName in Docker := "scala-crawler",
  dockerExposedPorts := Seq(8081)
)

lazy val web = Project(
  id = "web",
  base = file("web")
).
  settings(commonSettings,
    name := "web",
    mainClass in Compile := Some("crawler.web.main")
  ).settings(webDockerSettings).dependsOn(core)
  .enablePlugins(JavaAppPackaging, DockerPlugin)