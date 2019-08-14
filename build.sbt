
lazy val akkaHttpVersion = "10.1.8"
lazy val akkaVersion    = "2.5.21"
lazy val kafkaVersion = "2.0.0"
lazy val confluentVersion = "5.2.1"
lazy val jacksonVersion = "2.9.5"


val confluent: Seq[ModuleID] = Seq("io.confluent" % "kafka-schema-registry-client" % confluentVersion,
  "io.confluent" % "kafka-avro-serializer" % confluentVersion)

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "2.12.7"
    )),
    name := "scala-crawler",
    resolvers += Resolver.mavenLocal,
    resolvers += "Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases",
    resolvers += "Confluent Maven Repo" at "http://packages.confluent.io/maven/",
    resolvers += "jitpack" at "https://jitpack.io",
    resolvers += Resolver.bintrayRepo("hseeberger", "maven"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,

      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"            % "3.0.5"         % Test,


      "com.typesafe" % "config" % "1.3.2",

      "com.typesafe.slick" %% "slick" % "3.3.0",
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.3.0",
      "com.h2database" % "h2" % "1.4.187",

      "org.apache.kafka" %% "kafka" % kafkaVersion,
      "org.apache.kafka" % "kafka-clients" % kafkaVersion,
      "net.manub" %% "scalatest-embedded-kafka" % "2.0.0" % Test,


      "com.typesafe.akka" % "akka-stream-kafka_2.12" % "1.0.4",
      "com.sksamuel.avro4s" %% "avro4s-core" % "3.0.0-RC3",
    ),
    libraryDependencies ++= confluent

)