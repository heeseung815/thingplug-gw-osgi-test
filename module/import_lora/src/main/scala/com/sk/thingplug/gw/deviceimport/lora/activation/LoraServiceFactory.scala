package com.sk.thingplug.gw.deviceimport.lora.activation

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.mqtt.MqttSourceSettings
import com.typesafe.scalalogging.StrictLogging
import org.osgi.framework.{Bundle, BundleContext, ServiceFactory, ServiceRegistration}

/**
  * Created by kylee on 2017. 9. 12..
  */
class LoraServiceFactory(context:BundleContext)(implicit val actorSystem:ActorSystem, val materializer:ActorMaterializer) extends ServiceFactory[LoraService] with StrictLogging {
  override def getService(bundle: Bundle, registration: ServiceRegistration[LoraService]): LoraService = {
    //println(s"LoraServiceFactory.getService 1")
    logger.info(s"LoraServiceFactory.getService: context=${context.getBundle.getSymbolicName}, bundle=${bundle.getSymbolicName}, className=${bundle.getClass.getName}")
    val service = new LoraService(context.getBundle.getSymbolicName)
    service
  }

  override def ungetService(bundle: Bundle, registration: ServiceRegistration[LoraService], service: LoraService): Unit = {
    logger.info(s"LoraServiceFactory.ungetService")
  }
}
