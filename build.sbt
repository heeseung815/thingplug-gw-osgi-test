import sbt.Keys.{libraryDependencies, version}
import NativePackagerHelper._

version in ThisBuild := "2.0.0-SNAPSHOT"
organization in ThisBuild := "skt.thingplug"
scalaVersion in ThisBuild := Dependencies.Versions.scala
scalacOptions in ThisBuild += "-feature"


lazy val gwcommon = (project in file("common"))
  .enablePlugins(SbtOsgi)
  .settings(
    name := "gwcommon",
    libraryDependencies ++= Dependencies.gwcommon,
    osgiSettings,
    OsgiKeys.exportPackage := Seq("com.sk.thingplug.api"),
    OsgiKeys.bundleVersion := "0.1.0"
  )
lazy val gateway = (project in file("main"))
  .enablePlugins(SbtOsgi)
  .settings(
    name := "gateway",
    libraryDependencies ++= Dependencies.gwmain,
    osgiSettings,
    OsgiKeys.exportPackage := Seq("com.sk.thingplug.gw.activation", "com.sk.thingplug", "com.sk.thingplug.gw.service"),
    OsgiKeys.privatePackage := Seq("com.sk.thingplug.gw"),
    OsgiKeys.importPackage := Seq("com.sk.thingplug.api", "org.eclipse.paho.client.mqttv3", "org.eclipse.paho.client.mqttv3.persist", "org.slf4j", "akka.stream.alpakka.mqtt", "akka.stream.alpakka.mqtt.scaladsl", "com.typesafe.scalalogging", "org.osgi.framework.*", "com.typesafe.config", "org.osgi.service.cm", "org.osgi.util.tracker", "!sun.misc", "akka.*;version=\"[2.4,2.5.4]\"", "scala.*"),
    OsgiKeys.bundleActivator := Option("com.sk.thingplug.gw.activation.GraphActivator")
  ).dependsOn(gwcommon)
lazy val export_thingplug = (project in file("module/export_thingplug"))
  .enablePlugins(SbtOsgi)
  .settings(
      name := "export_thingplug",
      libraryDependencies ++= Dependencies.export_thingplug,
      osgiSettings,
      OsgiKeys.exportPackage := Seq("com.sk.thingplug.gw.export.thingplug.activation", "com.sk.thingplug.gw.export.thingplug"),
      OsgiKeys.privatePackage := Seq("com.sk.thingplug.gw", "com.sk.thingplug", "com.sk.thingplug.gw.export.thingplug", "com.sk.thingplug.gw.export.thingplug.mqtt"),
      OsgiKeys.importPackage := Seq("com.sk.thingplug.api", "spray.json", "javax.net", "javax.net.ssl", "org.slf4j",  "org.eclipse.paho.client.mqttv3", "org.eclipse.paho.client.mqttv3.persist", "akka.stream.alpakka.mqtt", "akka.stream.alpakka.mqtt.scaladsl", "com.typesafe.scalalogging", "org.osgi.framework.*", "com.typesafe.config", "org.osgi.service.cm", "org.osgi.util.tracker", "!sun.misc", "akka.*;version=\"[2.4,2.5.4]\"",  "scala.*"),
      OsgiKeys.bundleActivator := Option("com.sk.thingplug.gw.export.thingplug.activation.ThingPlugActivator")
  ).dependsOn(gwcommon, gateway)

lazy val export_datalake = (project in file("module/export_datalake"))
  .enablePlugins(SbtOsgi)
  .settings(
    name := "export_datalake",
    libraryDependencies ++= Dependencies.export_datalake,
    osgiSettings,
    OsgiKeys.exportPackage := Seq("com.sk.thingplug.gw.export.datalake.activation", "com.sk.thingplug.gw.export.datalake"),
    OsgiKeys.privatePackage := Seq("com.sk.thingplug.gw", "com.sk.thingplug", "com.sk.thingplug.gw.export.datalake", "com.sk.thingplug.gw.export.datalake.hdfs"),
    OsgiKeys.importPackage := Seq("com.sk.thingplug.api", "org.apache.hadoop", "org.apache.hadoop.conf", "spray.json", "org.slf4j", "com.typesafe.scalalogging", "org.osgi.framework.*", "com.typesafe.config", "org.osgi.service.cm", "org.osgi.util.tracker", "!sun.misc", "akka.*;version=\"[2.4,2.5.4]\"",  "scala.*"),
    OsgiKeys.bundleActivator := Option("com.sk.thingplug.gw.export.datalake.activation.DataLakeActivator")
  ).dependsOn(gwcommon, gateway)
lazy val main_test = (project in file("module/main_test"))
  .enablePlugins(SbtOsgi)
  .settings(
    name := "main_test",
    libraryDependencies ++= Dependencies.gwmain,
    osgiSettings,
    OsgiKeys.exportPackage := Seq("hscho.test.activation", "hscho.test", "hscho.test.service"),
    OsgiKeys.privatePackage := Seq("hscho.test"),
    OsgiKeys.importPackage := Seq("com.sk.thingplug.api", "org.slf4j", "com.typesafe.scalalogging", "org.osgi.framework.*", "com.typesafe.config", "org.osgi.service.cm", "org.osgi.util.tracker", "!sun.misc", "akka.*;version=\"[2.4,2.5.4]\"", "scala.*"),
    OsgiKeys.bundleActivator := Option("hscho.test.activation.TestActivator")
  ).dependsOn(gwcommon)

lazy val import_lora = (project in file("module/import_lora"))
  .enablePlugins(SbtOsgi)
  .settings(
    name := "import_lora",
    unmanagedJars in Compile += file("lib/onem2m.jar"),
    libraryDependencies ++= Dependencies.import_lora,
    osgiSettings,
    OsgiKeys.exportPackage := Seq("com.sk.thingplug.gw.deviceimport.lora.activation"),
    OsgiKeys.privatePackage := Seq("com.sk.thingplug.gw", "com.sk.thingplug", "com.sk.thingplug.gw.deviceimport.lora"),
    OsgiKeys.importPackage := Seq("com.sk.thingplug.api", "org.slf4j", "com.uangel.onem2m", "com.uangel.onem2m.resource", "com.uangel.onem2m.resource.lora_v1_0", "com.fasterxml.jackson.module.jaxb", "org.eclipse.paho.client.mqttv3", "akka.stream.alpakka.mqtt", "akka.stream.alpakka.mqtt.scaladsl", "com.typesafe.scalalogging", "org.osgi.framework.*", "com.typesafe.config", "org.osgi.service.cm", "org.osgi.util.tracker", "!sun.misc", "akka.*;version=\"[2.4,2.5.4]\"", "scala.*"),
    OsgiKeys.bundleActivator := Option("com.sk.thingplug.gw.deviceimport.lora.activation.LoraActivator")
  ).dependsOn(gwcommon, gateway)

lazy val flow_preprocessing_vib = (project in file("module/flow_preprocessing_vib"))
  .enablePlugins(SbtOsgi)
  .settings(
    name := "flow_preprocessing_vib",
    libraryDependencies ++= Dependencies.flow_preprocessing_vib,
    osgiSettings,
    OsgiKeys.exportPackage := Seq("com.sk.thingplug.gw.flow.preprocessing_vib.activation"),
    OsgiKeys.privatePackage := Seq("com.sk.thingplug.gw", "com.sk.thingplug"),
    OsgiKeys.importPackage := Seq("com.sk.thingplug.api", "org.slf4j", "com.typesafe.scalalogging", "org.osgi.framework.*", "com.typesafe.config", "org.osgi.service.cm", "org.osgi.util.tracker", "!sun.misc", "akka.*;version=\"[2.4,2.5.4]\"", "scala.*"),
    OsgiKeys.bundleActivator := Option("com.sk.thingplug.gw.flow.preprocessing_vib.activation.PreProcessingVibActivator")
  ).dependsOn(gwcommon, gateway)

lazy val flow_preprocessing_current = (project in file("module/flow_preprocessing_current"))
  .enablePlugins(SbtOsgi)
  .settings(
    name := "flow_preprocessing_current",
    libraryDependencies ++= Dependencies.flow_preprocessing_vib,
    osgiSettings,
    OsgiKeys.exportPackage := Seq("com.sk.thingplug.gw.flow.preprocessing_current.activation"),
    OsgiKeys.privatePackage := Seq("com.sk.thingplug.gw", "com.sk.thingplug"),
    OsgiKeys.importPackage := Seq("com.sk.thingplug.api", "org.slf4j", "com.typesafe.scalalogging", "org.osgi.framework.*", "com.typesafe.config", "org.osgi.service.cm", "org.osgi.util.tracker", "!sun.misc", "akka.*;version=\"[2.4,2.5.4]\"", "scala.*"),
    OsgiKeys.bundleActivator := Option("com.sk.thingplug.gw.flow.preprocessing_current.activation.PreProcessingCurrentActivator")
  ).dependsOn(gwcommon, gateway)

/*
lazy val root = (project in file("."))
  .enablePlugins(UniversalPlugin, JavaAppPackaging, UniversalDeployPlugin)
  .settings(
    name := "gateway",
    resourceDirectory in Compile := baseDirectory( _ / "conf" ).value,
    libraryDependencies ++= Dependencies.akka
      ++ Dependencies.guice
      ++ Dependencies.akkaStreamKafka
      ++ Dependencies.mqttConnector
  )
  // Packaging Settings
  .settings(
  javaOptions in Runtime ++= Seq(
      "-DAPP_HOME=./",
      "-Dconfig.file=./conf/gateway.conf",
      "-Dscala.concurrent.context.maxThreads=128"
    )
  )                 
  .settings(
    mappings in Universal ++= directory("bin"),
    mappings in Universal ++= directory("lib"),
    mappings in Universal ++= directory("conf"),
    mappings in (Compile, packageDoc) := Seq(),
    mainClass in Compile := Some("com.sk.thingplug.gw.Gateway"),
    packageName in Universal := s"thingplug-gateway-${(version in ThisBuild).value}",
    executableScriptName := "thingplug-gateway",
    scriptClasspath := Seq("${app_home}/../conf") ++ scriptClasspath.value,
    bashScriptConfigLocation := Some("${app_home}/../conf/gateway.ini"),
    bashScriptExtraDefines ++= Seq(
      addJava "-DAPP_HOME=$(dirname $app_home)" """,
      """addJava "-Dconfig.file=$(dirname $app_home)/conf/gateway.conf" """,
      """addJava "-Dlogger.file=$(dirname $app_home)/conf/logback.xml" """,
      """addJava "-Dpidfile.path=$(dirname $app_home)/bin/thingplug-gateway.pid" """
    )
  )
  ///////////////////////////////////////////////////
  // Test configuration
  .settings(
  parallelExecution in Test := false
)
*/
