package com.sk.thingplug.gw.kafka

import com.google.inject.AbstractModule
import com.typesafe.scalalogging.StrictLogging

/**
  * 2017. 6. 7. - Created by Kwon, Yeong Eon
  */
class TtvKafkaConsumerModule extends AbstractModule with StrictLogging {

  override def configure(): Unit = {
    logger.debug("============>")
  }
}
