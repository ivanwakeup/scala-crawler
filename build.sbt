import Dependencies._


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

lazy val web = Project(
  id = "web",
  base = file("web")
).
  settings(commonSettings,
    name := "web"
  ).dependsOn(core)