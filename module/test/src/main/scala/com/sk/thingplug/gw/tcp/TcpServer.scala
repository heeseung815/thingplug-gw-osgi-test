package com.sk.thingplug.gw.tcp

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{Flow, Framing, Sink, Tcp}
import akka.util.ByteString
import com.sk.thingplug.gw.GatewayConfig
import com.sk.thingplug.gw.stages.sink.MqttPublisher
import com.typesafe.scalalogging.StrictLogging

/**
  * 2017. 6. 13. - Created by kwonkr01
  */
@Deprecated
class TcpServer extends StrictLogging {
  implicit val system = ActorSystem("TcpServer")
  implicit val ec = system.dispatcher
  implicit val m = ActorMaterializer.create(system)

  val config = GatewayConfig.config
  private val tcpHostname: String = if (config.hasPath("thingplug.tcp.hostname")) config.getString("thingplug.tcp.hostname") else "localhost"
  private val tcpPort: Int = if (config.hasPath("thingplug.tcp.port")) config.getInt("thingplug.tcp.port") else 12345

  def init(): Unit = {
    logger.info("Starting tcp server...")
    startTcpServer(tcpHostname, tcpPort)
  }

  def startTcpServer(address: String, port: Int)(implicit system: ActorSystem, materializer: Materializer): Unit  = {
    val mqttSinkGraph = MqttPublisher
    val mqttSink = Sink.fromGraph(MqttPublisher)
    val server = Tcp().bind(tcpHostname, tcpPort)

    server.runForeach{ (conn: Tcp.IncomingConnection) =>
      println(s"Client connected ${conn.remoteAddress}")
      val mqttFlow = Flow[ByteString]
        .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 256, allowTruncation = true))
        .map(_.utf8String).map { msg ⇒ logger.debug(s"Server received: $msg"); msg }
        .alsoTo(mqttSink).map {ByteString(_)}

      // "handleWith" method takes "flow" stage as an argument but it returns nothing. So, "flow" executes "sink" stage to finish a stream internally.
      conn.handleWith(mqttFlow)
    }
  }

  def shutdown(): Unit = {
    logger.info("Shutdown tcp server.")
  }
}
