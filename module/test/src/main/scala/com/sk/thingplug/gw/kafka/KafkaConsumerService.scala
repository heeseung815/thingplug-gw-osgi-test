package com.sk.thingplug.gw.kafka

import akka.actor.ActorSystem
import akka.kafka._
import akka.kafka.scaladsl.Consumer
import akka.stream.alpakka.mqtt.{MqttConnectionSettings, MqttMessage, MqttQoS}
import akka.stream.alpakka.mqtt.scaladsl.MqttSink
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Zip, ZipWith}
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.util.ByteString
import com.google.inject.Inject
import com.sk.thingplug.gw.GatewayConfig
import com.sk.thingplug.gw.stages.flow.{TtvConverter, TtvToThingplugDataConverter, XmlConverter}
import com.sk.thingplug.gw.stages.sink.{MqttPublisher, MqttPublisherAlpakka}
import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import scala.concurrent.Future

/**
  * Created by Cho on 2017-06-12.
  */
class KafkaConsumerService @Inject()(implicit system:ActorSystem, m:ActorMaterializer) extends StrictLogging {
  //val system = ActorSystem("KafkaConsumerService")
  implicit val ec = system.dispatcher
  //implicit val m = ActorMaterializer.create(system)

  val config          = GatewayConfig.config
  val kafkaHostname   = if (config.hasPath("thingplug.kafka.server.hostname")) config.getString("thingplug.kafka.server.hostname") else "localhost"
  val kafkaPort       = if (config.hasPath("thingplug.kafka.server.port")) config.getInt("thingplug.kafka.server.port") else 9092
  val topic           = if (config.hasPath("thingplug.kafka.consumer.topic")) config.getString("thingplug.kafka.consumer.topic") else "test"
  val partition       = if (config.hasPath("thingplug.kafka.consumer.partition")) config.getInt("thingplug.kafka.consumer.partition") else 0
  val consumerGroupId = if (config.hasPath("thingplug.kafka.consumer.groupid")) config.getString("thingplug.kafka.consumer.groupid") else "group1"

  var consumerSettings: ConsumerSettings[Array[Byte], String] = _

  def getMqttTopic(kafkaTopic: String): Future[String] = Future.successful(s"mqtt_$kafkaTopic")
  def init(): Unit = {
    logger.info(s"Starting KafkaConsumerService... $system $m")

    /*
    val subscription = Subscriptions.assignment(
      new TopicPartition("dev1", partition)
    )
    */
    //해당 패턴의 존재하는 Topic에 대한 subscription 생성
    val subscription = Subscriptions.topicPattern("dev[1-9][0-9]?[0-9]?")

    //val subscription = Subscriptions.topicPattern("dev*")

    consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
      .withBootstrapServers(kafkaHostname + ":" + kafkaPort)
      .withGroupId(consumerGroupId)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")

//    val kafkaSource = Consumer.plainSource(consumerSettings, subscription)
//    val xmlConverterFlow = Flow.fromGraph(new XmlConverter[ConsumerRecord[Array[Byte], String]])
//    val ttvConverterFlow = Flow.fromGraph(new TtvConverter[ConsumerRecord[Array[Byte], String]])
//    val mqttSink = Sink.fromGraph(MqttPublisher)

    RunnableGraph.fromGraph(GraphDSL.create() {
      implicit builder =>
        import GraphDSL.Implicits._
        val kafkaSource = Consumer.plainSource(consumerSettings, subscription)
        // kafka message에서 topic을 가져와서 mqtt topic으로 변환.
        val getTopic = Flow[ConsumerRecord[Array[Byte], String]]
          .map[String]{ x => x.topic()}
          .mapAsync(1){ t => getMqttTopic(t)}

        // kafka message에서 payload에 대해 ttv validation 후 파싱하여 json으로 변환.
        val ttvDecoder = Flow[ConsumerRecord[Array[Byte], String]]
          .filter( x => TtvConverter.Validate(x.value))
          .map[Array[Map[String, Any]]]{ x => TtvConverter.parseTtv(x.value)}

        //ttv를 thingplug 데이터 형식으로 변환  (EX)"Temperature" : 35.6
        val ttvToThingplugDataConverter = Flow[Array[Map[String, Any]]]
          .map[String]{ x => TtvToThingplugDataConverter.convertThingplugData(x)}

        //.recover
        val bcast = builder.add(Broadcast[ConsumerRecord[Array[Byte], String]](2))

        // topic과 payload를 받아서 mqttmessage로 변환.
        val zip = builder.add(ZipWith[String, String, MqttMessage]{
          (topic, payload) => MqttMessage(topic,ByteString(payload))
        })

        val mqttSink = MqttPublisherAlpakka()

        kafkaSource ~> bcast ~> getTopic ~> zip.in0
                       bcast ~> ttvDecoder ~> ttvToThingplugDataConverter ~> zip.in1
        zip.out ~> mqttSink
        ClosedShape

    }).run()
  }

  def shutdown(): Unit = {
    logger.info("Shutdown KafkaConsumerService.")
  }
}