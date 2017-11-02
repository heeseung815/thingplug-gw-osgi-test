package module.mqtt

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.mqtt.scaladsl.{MqttSink, MqttSource}
import akka.stream.alpakka.mqtt.{MqttConnectionSettings, MqttMessage, MqttQoS, MqttSourceSettings}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.testkit.TestKit
import akka.util.ByteString
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

/**
  * Created by kwonkr01 on 2017-06-13.
  */
class MqttTest extends TestKit(ActorSystem("MqttTest"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with ScalaFutures {

  implicit val defaultPatience =
    PatienceConfig(timeout = 5.seconds, interval = 100.millis)

  implicit val mat = ActorMaterializer()
  val connectionSettings = MqttConnectionSettings(
    "tcp://localhost:1883",
    "test-client",
    new MemoryPersistence
  )

  val topic = "topic"
  val secureTopic = "sink-spec/secure-topic1"

  val sourceSettings = connectionSettings.withClientId(clientId = "sink-spec/source")
  val sinkSettings = connectionSettings.withClientId(clientId = "sink-spec/sink")

  "mqtt sink" should {
    "send one message to a topic" in {
      val msg = MqttMessage(topic, ByteString("{ \"name\":\"John\", \"age\":30, \"car\":null }"))

      val (subscribed, message) =
        MqttSource(MqttSourceSettings(sourceSettings, Map(topic -> MqttQoS.atLeastOnce)), 8)
          .toMat(Sink.head)(Keep.both)
          .run()

      whenReady(subscribed) { _ =>
        Source.single(msg).runWith(MqttSink(sinkSettings, MqttQoS.atLeastOnce))
      }

      message.futureValue shouldBe msg
    }
  }
}
