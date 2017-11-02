package com.sk.thingplug.gw.coap

import javax.inject.Inject

import akka.actor.ActorSystem
import com.google.inject.Injector
import com.sk.thingplug.gw.GatewayConfig
import com.typesafe.scalalogging.StrictLogging

/**
  * 2017. 6. 8. - Created by Kwon, Yeong Eon
  */
class CoapServer @Inject()(actorSystem: ActorSystem, injector: Injector) extends StrictLogging {
  val hostname: String = GatewayConfig.config.getString("thingplug.coap.hostname")
  val port: Int = GatewayConfig.config.getInt("thingplug.coap.port")

  def init(): Unit = {
    logger.info("Starting Coap server...")
  }

  def shutdown(): Unit = {
    logger.info("Shutdown Coap server.")
  }

}
