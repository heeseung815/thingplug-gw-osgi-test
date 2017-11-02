package com.sk.thingplug.gw.tcp

import com.google.inject.{AbstractModule, Provider}
import com.sk.thingplug.{BasicModule, CoreModule, MainContext}
import com.typesafe.scalalogging.StrictLogging

/**
  * 2017. 6. 7. - Created by Kwon, Yeong Eon
  */
class DefaultTcpModule extends BasicModule with CoreModule with StrictLogging {

  private var provider: Provider[TcpServer] = _

  private var service: TcpServer = _

  override def configure(): Unit = {
    bind(classOf[DefaultTcpModule]).toInstance(this)
    provider = getProvider(classOf[TcpServer])
  }

  override def start(): Unit = {
    service = provider.get()
    service.init()
    logger.debug(s"Start TCP Module ${service.toString}")
  }

  override def stop(): Unit = {
    logger.debug("Shutdown TCP Module")
    service.shutdown()
  }

  override def name: String = MainContext.tcpModuleName
}
