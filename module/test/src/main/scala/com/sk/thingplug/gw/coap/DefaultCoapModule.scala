package com.sk.thingplug.gw.coap

import com.google.inject.Provider
import com.sk.thingplug.{BasicModule, CoreModule, MainContext}
import com.typesafe.scalalogging.StrictLogging

/**
  * 2017. 6. 7. - Created by Kwon, Yeong Eon
  */
class DefaultCoapModule extends BasicModule with CoreModule with StrictLogging {

  private var provider: Provider[CoapServer] = _

  private var service: CoapServer = _

  override def configure(): Unit = {
    provider = getProvider(classOf[CoapServer])
    bind(classOf[CoapServer]).asEagerSingleton()
  }

  override def start(): Unit = {
    service = provider.get()
    service.init()
    logger.debug(s"Start coap Module ${service.toString}")
  }

  override def stop(): Unit = {
    logger.debug("Shutdown coap Module")
    service.shutdown()
  }

  override def name: String = MainContext.coapModuleName
}
