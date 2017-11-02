package com.sk.thingplug.gw.stages.sink

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler}
import com.sk.thingplug.gw.GatewayConfig
import com.typesafe.scalalogging.StrictLogging
import org.eclipse.paho.client.mqttv3._
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

/**
  * Created by kwonkr01 on 2017-06-16.
  */
object MqttPublisher extends GraphStage[SinkShape[String]] with StrictLogging {
  implicit val system = ActorSystem("MqttPublisher")
  implicit val ec = system.dispatcher
  implicit val m = ActorMaterializer.create(system)

  val config = GatewayConfig.config
  val mqttBrokerHostname: String = if (config.hasPath("thingplug.mqtt.hostname")) config.getString("thingplug.mqtt.hostname") else "localhost"
  val mqttPort: Int = if (config.hasPath("thingplug.mqtt.port")) config.getInt("thingplug.mqtt.port") else 1883
  val mqttTopic: String = if (config.hasPath("thingplug.mqtt.topic")) config.getString("thingplug.mqtt.topic") else "thingplug"
  val mqttClientId: String = if (config.hasPath("thingplug.mqtt.clientId")) config.getString("thingplug.mqtt.clientId") else "thingplug-mqttclient"
  var client: MqttClient = null

  val in = Inlet[String]("mqtt.in")
  override val shape: SinkShape[String] = SinkShape(in)

  // Constructor
  {
    val persistence = new MemoryPersistence
    client = new MqttClient("tcp://"+ mqttBrokerHostname + ":" + mqttPort, mqttClientId, persistence)
    client.connect()
  }

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    setHandler(in, new InHandler {
      override def onPush() = grab(in) match {
        case msg: String ⇒
          if (client.isConnected)
          {
            val msgTopic = client.getTopic(mqttTopic)
            val message = new MqttMessage(msg.getBytes("utf-8"))
            msgTopic.publish(message)
            pull(in)
          }
          else
            logger.error(s"MQTT connection is not established. broker url: ${"tcp://:"+ mqttBrokerHostname + ":" + mqttPort}")
        case _ =>
          logger.error("Invalid data")
          completeStage()
      }
    })

    // This requests one element at the Sink startup.
    override def preStart(): Unit = pull(in)
  }

  def shutdown(): Unit = {
    if (client.isConnected)
      client.disconnect
  }
}
