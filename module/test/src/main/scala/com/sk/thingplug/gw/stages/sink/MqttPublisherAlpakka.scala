package com.sk.thingplug.gw.stages.sink

import akka.Done
import akka.stream.alpakka.mqtt.{MqttConnectionSettings, MqttMessage, MqttQoS}
import akka.stream.alpakka.mqtt.scaladsl.MqttSink
import akka.stream.scaladsl.Sink
import com.sk.thingplug.gw.GatewayConfig
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import scala.concurrent.Future

/**
  * Created by kylee on 2017. 7. 11..
  */
object MqttPublisherAlpakka  {
  val config = GatewayConfig.config
  val mqttBrokerHostname: String = if (config.hasPath("thingplug.mqtt.hostname")) config.getString("thingplug.mqtt.hostname") else "localhost"
  val mqttPort: Int = if (config.hasPath("thingplug.mqtt.port")) config.getInt("thingplug.mqtt.port") else 1883
  val mqttClientId: String = if (config.hasPath("thingplug.mqtt.clientId")) config.getString("thingplug.mqtt.clientId") else "thingplug-mqttclient"

  val connectionSettings = MqttConnectionSettings(
    "tcp://"+ mqttBrokerHostname + ":" + mqttPort,
    mqttClientId,
    new MemoryPersistence
  )

  def apply(): Sink[MqttMessage, Future[Done]] =
   MqttSink(connectionSettings, MqttQoS.AtLeastOnce)
}
