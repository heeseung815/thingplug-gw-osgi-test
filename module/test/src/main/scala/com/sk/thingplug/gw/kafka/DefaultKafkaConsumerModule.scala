package com.sk.thingplug.gw.kafka

import com.google.inject.{AbstractModule, Provider}
import com.sk.thingplug.{BasicModule, CoreModule, MainContext}
import com.typesafe.scalalogging.StrictLogging

/**
  * 2017. 6. 7. - Created by Kwon, Yeong Eon
  */
class DefaultKafkaConsumerModule extends BasicModule with CoreModule with StrictLogging {

  private var provider: Provider[KafkaConsumerService] = _

  private var service: KafkaConsumerService = _

  override def configure(): Unit = {
    bind(classOf[DefaultKafkaConsumerModule]).toInstance(this)
    provider = getProvider(classOf[KafkaConsumerService])
  }

  override def start(): Unit = {
    service = provider.get()
    service.init()
    logger.debug(s"Start KafkaConsumer Module ${service.toString}")
  }

  override def stop(): Unit = {
    logger.debug("Shutdown KafkaConsumer Module")
    service.shutdown()
  }

  override def name: String = MainContext.kafkaModuleName
}
