package com.sk.thingplug.gw.deviceimport.lora.activation

import akka.NotUsed
import akka.actor.ActorSystem
import akka.osgi.ActorSystemActivator
import akka.stream.ActorMaterializer
import akka.stream.alpakka.mqtt.{MqttMessage, MqttSourceSettings}
import com.sk.thingplug.api.{GraphData, ImportService}
import com.typesafe.scalalogging.StrictLogging
import org.osgi.framework.{BundleActivator, BundleContext, ServiceRegistration}

/**
  * Created by kylee on 2017. 9. 13..
  */
class LoraActivator extends ActorSystemActivator with StrictLogging {
  /*
  override def start(context: BundleContext): Unit = {
    println(s"LoraActivator.start: name=${classOf[ImportService[Any, Any, Any, Any]].getName}")
    context.registerService(classOf[ImportService[Any, Any, Any, Any]].getName,
      new LoraServiceFactory(context), null)
    println(s"LoraActivator.start:Done")
  }
  */

  var registration:ServiceRegistration[_] = null

  override def stop(context: BundleContext): Unit = {
    logger.info(s"LoraActivator.stop: ${context.toString}")
    registration.unregister()
    logger.info(s"LoraActivator.stop: Done")
  }

  override def configure(context: BundleContext, system: ActorSystem): Unit = {
    logger.info(s"LoraActivator.configure: ${system.toString}")
    implicit val materializer:ActorMaterializer = ActorMaterializer.create(system)
    implicit val s:ActorSystem = system
    registration = context.registerService(classOf[ImportService[Any, Any, Any, Any]].getName,
      new LoraServiceFactory(context), null)
    //logger.info(s"LoraActivator.configure:Done")
  }
}
