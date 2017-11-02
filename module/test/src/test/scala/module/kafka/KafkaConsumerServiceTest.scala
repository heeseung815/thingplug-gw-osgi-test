package module.kafka

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream._
import akka.stream.alpakka.mqtt.{MqttConnectionSettings, MqttMessage, MqttQoS, MqttSourceSettings}
import akka.stream.alpakka.mqtt.scaladsl.{MqttSink, MqttSource}
import akka.stream.scaladsl.{Flow, GraphDSL, Keep, RunnableGraph, Sink, Source}
import akka.util.ByteString
import com.sk.thingplug.gw.GatewayConfig
import com.sk.thingplug.gw.stages.flow.{TtvConverter, XmlConverter}
import com.sk.thingplug.gw.stages.sink.MqttPublisher
import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.{JSONObject, XML}

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.xml.XML.loadString

/**
  * Created by Cho on 2017-06-13.
  */
trait KafkaConsumerServiceTest extends StrictLogging {
  val system = ActorSystem("KafkaConsumerServiceTest")
  implicit val ec = system.dispatcher
  implicit val m = ActorMaterializer.create(system)

  val kafkaServer = "192.168.1.165:9092"
  val consumerGroupId = "group1"

  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers(kafkaServer)
    .withGroupId(consumerGroupId)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
//    .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
  // auto.offset.reset (default: latest)  - earlist, latest, none
  // enable.auto.commit (default: true)   - true, false

  val config = GatewayConfig.config
  val mqttBrokerHostname = if (config.hasPath("thingplug.mqtt.hostname")) config.getString("thingplug.mqtt.hostname") else "localhost"
  val mqttPort = if (config.hasPath("thingplug.mqtt.port")) config.getInt("thingplug.mqtt.port") else 1883
  val mqttConnectionSettings = MqttConnectionSettings("tcp://:"+ mqttBrokerHostname + ":" + mqttPort, "thingplug-mqtt-client", new MemoryPersistence)
  val mqttTopic: String = if (config.hasPath("thingplug.mqtt.topic")) config.getString("thingplug.mqtt.topic") else "thingplug"

  def printFetchedMessage(record: ConsumerRecord[Array[Byte], String]): Future[Done] = {
    logger.info("fetched message: " + record.value)
    Future.successful(Done)
  }

  def printFetchedMessage2(record : String): Future[Done] = {
    logger.info("fetched message: " + record)
    Future.successful(Done)
  }

  def terminateWhenDone(result: Future[Done]): Unit = {
    result.onComplete {
      case Failure(e) =>
        system.log.error(e, e.getMessage)
        system.terminate()
      case Success(_) => system.terminate()
    }
  }
}

object ConsumeXmlMessagesTest extends KafkaConsumerServiceTest {

  // Send a xml message to kafka server
  // /home/hscho/KAFKA/kafka_2.11-0.10.2.0/bin/kafka-console-producer.sh --broker-list 192.168.1.165:9092 --topic topic8 < /home/hscho/KAFKA/kafka_2.11-0.10.2.0/test/test.xml

  def main(args: Array[String]): Unit = {
    val partition = 0
    val subscription = Subscriptions.assignment(
      new TopicPartition("test2", partition)
    )

    /** mqtt settings **/
//    val sourceSettings = mqttConnectionSettings.withClientId(clientId = "mqtt-source")
//    val subscribed = MqttSource(MqttSourceSettings(sourceSettings, Map(mqttTopic -> MqttQoS.atLeastOnce)), 8).toMat(Sink.head)(Keep.both).run()
//    subscribed._1.onComplete {
//      case Success(_) => logger.info("MQTT connection is established")
//      case Failure(ex) => logger.error(s"MQTT connection error , ${mqttBrokerHostname}:${mqttPort}, ${ex.getMessage}")
//    }

//    val source: Source[ConsumerMessage.CommittableMessage[Array[Byte], String], Consumer.Control] = Consumer.committableSource(consumerSettings, Subscriptions.topics("topic8"))
//    val parseFlow = Flow[ConsumerMessage.CommittableMessage[Array[Byte], String]].mapAsync(1){msg => parseXmlToJson(msg.record.value()).map(_ => msg)}
//    val offsetFlow = parseFlow.mapAsync(1){msg => msg.committableOffset.commitScaladsl()}
//    val done = source.via(offsetFlow).runWith(Sink.ignore)

//    val flow: Flow[ConsumerRecord[Array[Byte], String], JSONObject, NotUsed] = Flow[ConsumerRecord[Array[Byte], String]].mapAsync(1)(parseXmlToJson)
//    val sink: Sink[JSONObject, Future[Done]] = Sink.foreach[JSONObject](msg => logger.info("Parsing completed: " + msg))
//    val done: Future[Done] = source.via(flow).runWith(sink)

//    val flow: Flow[ConsumerRecord[Array[Byte], String], JSONObject, NotUsed] = Flow[ConsumerRecord[Array[Byte], String]].map(parseXmlToJson)
//    val sink: Sink[JSONObject, Future[Done]] = Sink.foreach[JSONObject](sendMqttMessage)

    /** success example **/
//    val source = Consumer.plainSource(consumerSettings, subscription)
//    val sink = Sink.foreach[Any](msg => logger.info("Parsing completed: " + msg))
//    val done = source.via(Parser.xmlToJson[ConsumerRecord[Array[Byte], String]]).runWith(sink)
//    terminateWhenDone(done)

    /** test example **/
//    val sink = Sink.foreach[Any](msg => logger.info("Parsing completed: " + msg))
//    val sink = new TestSink[Any]
//    val done = source.via(flow).runWith(sink)
//    terminateWhenDone(done)

    /** kafka simple example **/
//    val source: Source[ConsumerRecord[Array[Byte], String], Consumer.Control] = Consumer.plainSource(consumerSettings, subscription)
//    source.runWith(Sink.foreach(msg => logger.info("kafka msg: " + msg.value)))

    val kafkaSource = Consumer.plainSource(consumerSettings, subscription)
    val xmlConverterFlow: Flow[ConsumerRecord[Array[Byte], String], String, NotUsed] = Flow.fromGraph(new XmlConverter[ConsumerRecord[Array[Byte], String]])
    val ttvConverterFlow: Flow[ConsumerRecord[Array[Byte], String], Array[Map[String, Any]], NotUsed] = Flow.fromGraph(new TtvConverter[ConsumerRecord[Array[Byte], String]])
    val mqttSink = Sink.fromGraph(MqttPublisher)
//    val simpleSink = Sink.foreach[String](msg => logger.info("Parsing completed: " + msg))
//    kafkaSource.via(converterFlow).runWith(simpleSink)
//    kafkaSource.via(converterFlow).runWith(mqttSink)

    RunnableGraph.fromGraph(GraphDSL.create() {
      implicit builder =>
      import GraphDSL.Implicits._
//        kafkaSource ~> xmlConverterFlow ~> mqttSink
//        kafkaSource ~> ttvConverterFlow ~> mqttSink
        ClosedShape
    }).run()

  }

  def parseXmlToJson(record: ConsumerRecord[Array[Byte], String]): JSONObject = {
    XML.toJSONObject(getXmlChild(record.value))
  }

  // To remove root element from Xml
  def getXmlChild(xmlString: String): String = {
    loadString(xmlString).child.mkString
  }

  def sendMqttMessage(msg: JSONObject): Future[Done] = {
    val mqttMsg = MqttMessage(mqttTopic, ByteString(msg.toString))
    val sinkSettings = mqttConnectionSettings.withClientId(clientId = "mqtt-sink")
    Source.single(mqttMsg).runWith(MqttSink(sinkSettings, MqttQoS.atLeastOnce))
  }
}

object Parser {
  def xmlToJson[T]: Flow[T, JSONObject, NotUsed] = {
    Flow[T].map {
      case record: ConsumerRecord[Array[Byte], String] => XML.toJSONObject(loadString(record.value).child.mkString)
      case _ => ???
    }
  }
}
