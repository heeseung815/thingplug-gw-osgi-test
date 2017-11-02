package com.sk.thingplug.gw

//import com.google.inject.{AbstractModule, Guice}
//import com.sk.thingplug.{BasicModule, RootServiceComponent}
//import com.typesafe.scalalogging.StrictLogging

import com.typesafe.scalalogging.StrictLogging

import scala.collection.JavaConverters._

/**
  * 2017. 6. 7. - Created by Kwon, Yeong Eon
  */
object Gateway {

//  banner()
//  logger.info("Starting")
//
//  val moduleList = GatewayConfig.config.getStringList("thingplug.modules.enabled").asScala
//  logger.info(s"$moduleList")
//  val classLoader = getClass.getClassLoader
//
//  val modulesEnabled = moduleList.map { m => classLoader.loadClass(m).newInstance().asInstanceOf[AbstractModule] }
//
//  val modules = new GatewayModule() +: modulesEnabled
//  implicit val injector = Guice.createInjector(modules:_*)
//
//  modules.foldLeft(0)(
//    (i,module) =>
//      module match {
//        case mod: BasicModule =>
//          logger.debug(s"module: ${mod.name} starting")
//          mod.start()
//          i + 1
//      }
//  )
//
//  logger.debug("...")
//  val root = injector.getInstance(classOf[RootServiceComponent])
//  root.start()
//
//  sys.addShutdownHook {
//    root.stop()
//    modules.foldRight(0)(
//      (module,i) =>
//        module match {
//          case mod: BasicModule =>
//            mod.stop()
//            logger.debug(s"module: ${mod.name} stopped")
//            i + 1
//        }
//    )
//  }

   def banner(): Unit = {
    println(
      """
        | _____ _     _             ____  _                ____       _
        ||_   _| |__ (_)_ __   __ _|  _ \| |_   _  __ _   / ___| __ _| |_ _____      ____ _ _   _
        |  | | | '_ \| | '_ \ / _` | |_) | | | | |/ _` | | |  _ / _` | __/ _ \ \ /\ / / _` | | | |
        |  | | | | | | | | | | (_| |  __/| | |_| | (_| | | |_| | (_| | ||  __/\ V  V / (_| | |_| |
        |  |_| |_| |_|_|_| |_|\__, |_|   |_|\__,_|\__, |  \____|\__,_|\__\___| \_/\_/ \__,_|\__, |
        |                     |___/               |___/                                     |___/
        |
      """.stripMargin)
  }
}
