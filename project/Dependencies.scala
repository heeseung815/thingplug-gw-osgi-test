import sbt._

object Dependencies {

  object Versions {
    val scala = "2.12.2"
    val akka = "2.5.4"
    val phantom = "2.9.2"
    val akka_cassandra = "0.54"
  }

  val akka: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-actor" % Versions.akka,
    "com.typesafe.akka" %% "akka-remote" % Versions.akka,
    "com.typesafe.akka" %% "akka-slf4j" % Versions.akka,
    // For xml and json
    "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
    "org.json" % "json" % "20170516",
    "org.json4s" %% "json4s-native" % "3.5.2",
    "com.typesafe.play" %% "play-json" % "2.6.0",
    // Logging
    "ch.qos.logback" % "logback-classic" % "1.2.3", // 01-Apr-2017 updated
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
    // Alpakka
    "com.lightbend.akka" %% "akka-stream-alpakka-mqtt" % "0.10",
    // Testing
    "com.typesafe.akka" %% "akka-testkit" % Versions.akka % Test,
    "org.scalatest" %% "scalatest" % "3.0.1" % Test
  )

  val akka_actor = "com.typesafe.akka" %% "akka-actor" % Versions.akka force()
  val akka_osgi = "com.typesafe.akka" %% "akka-osgi" % Versions.akka  exclude("org.osgi.core", "org.osgi.compendium") changing()
  val akka_remote = "com.typesafe.akka" %% "akka-remote" % Versions.akka              changing()
  val akka_cluster = "com.typesafe.akka" %% "akka-cluster" % Versions.akka force()
  val config = "com.typesafe" % "config" % "1.3.1"
  val scalalogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
  //val play = "com.typesafe.play" %% "play-json" % "2.6.0"
  val spray = "io.spray" %%  "spray-json" % "1.3.3"

  val osgiCore = "org.osgi" % "org.osgi.core" % "4.3.0" % "provided"
  val osgiCompendium = "org.osgi" % "org.osgi.compendium" % "4.3.0"
  val alpakka = "com.lightbend.akka" %% "akka-stream-alpakka-mqtt" % "0.10"

  //hscho
  val hadoop = "org.apache.hadoop" % "hadoop-client" % "2.8.1"

  val gwcommon = Seq(akka_actor, akka_osgi, akka_remote)
  val gwmain = Seq(akka_actor, akka_osgi, akka_remote, config, scalalogging, osgiCore, osgiCompendium, alpakka)
  val export_thingplug = Seq(akka_actor, akka_osgi, akka_remote, config, scalalogging, osgiCore, osgiCompendium, spray)
  val import_lora = Seq(akka_actor, akka_osgi, akka_remote, config, scalalogging, osgiCore, osgiCompendium, alpakka)
  val flow_preprocessing_vib = Seq(akka_actor, akka_osgi, akka_remote, config, scalalogging, osgiCore, osgiCompendium)

  //hscho
  val export_datalake = Seq(akka_actor, akka_osgi, akka_remote, config, scalalogging, osgiCore, osgiCompendium, spray, hadoop)
  val import_test = Seq(akka_actor, akka_osgi, akka_remote, config, scalalogging, osgiCore, osgiCompendium)


  val guice: Seq[ModuleID] = Seq(
    //"com.google.inject" % "guice" % "4.1.0"
    "net.codingwell" %% "scala-guice" % "4.1.0"
  )

  val akkaStreamKafka: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-stream-kafka" % "0.16"
  )

  val mqttConnector: Seq[ModuleID] = Seq(
    "com.lightbend.akka" % "akka-stream-alpakka-mqtt_2.12" % "0.9"
  )
}