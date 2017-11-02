package module.tcp

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.alpakka.mqtt.scaladsl.{MqttSink, MqttSource}
import akka.stream.alpakka.mqtt.{MqttConnectionSettings, MqttMessage, MqttQoS, MqttSourceSettings}
import akka.stream.scaladsl.{Concat, Flow, Framing, GraphDSL, Keep, RunnableGraph, Sink, Source, Tcp}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.util.ByteString
import akka.{Done, NotUsed}
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by kwonkr01 on 2017-06-13.
  */
object TcpTest extends ScalaFutures {

  def main(args: Array[String]): Unit = {
    implicit val sys = ActorSystem("Server")
    implicit val materializer = ActorMaterializer()
    mkServer("localhost", 12345)
    val clientSystem = ActorSystem("Client")
    client(clientSystem, "localhost", 12345)
  }

  def mkServer(address: String, port: Int)(implicit system: ActorSystem, materializer: Materializer): Unit = {
    val topic = "topic"
    import system.dispatcher

    implicit val mat = ActorMaterializer()
    val connectionSettings = MqttConnectionSettings(
      "tcp://localhost:1883",
      "test-client",
      new MemoryPersistence
    )

    val sourceSettings = connectionSettings.withClientId(clientId = "sink-spec/source")
    val sinkSettings = connectionSettings.withClientId(clientId = "sink-spec/sink")

    val (subscribed, message) =
      MqttSource(MqttSourceSettings(sourceSettings, Map(topic -> MqttQoS.atLeastOnce)), 8)
        .toMat(Sink.head)(Keep.both)
        .run()

    val closeConnection = new GraphStage[FlowShape[String, String]] {
      val in = Inlet[String]("closeConnection.in")
      val out = Outlet[String]("closeConnection.out")

      override val shape = FlowShape(in, out)

      override def createLogic(inheritedAttributes: Attributes) = new GraphStageLogic(shape) {
        setHandler(in, new InHandler {
          override def onPush() = grab(in) match {
            case "q" ⇒
              push(out, "BYE")
              completeStage()
            case msg ⇒
              push(out, s"Server hereby responds to message: $msg\n")
          }
        })
        setHandler(out, new OutHandler {
          override def onPull() = pull(in)
        })
      }
    }

    val mqttFlowStage = new GraphStage[FlowShape[String, String]] {
      val in = Inlet[String]("tcpMessage.in")
      val out = Outlet[String]("tcpMessage.out")
      override val shape = FlowShape(in, out)

      override def createLogic(inheritedAttributes: Attributes) = new GraphStageLogic(shape) {
        setHandler(in, new InHandler {
          override def onPush() = grab(in) match {
            case msg ⇒
              println(s"mqttStage've got a message: $msg\n")
              val mqttMsg = MqttMessage(topic, ByteString(msg))
              whenReady(subscribed) { _ =>
                Source.single(mqttMsg).runWith(MqttSink(sinkSettings, MqttQoS.atLeastOnce))
              }
              push(out, msg)
          }
        })
        setHandler(out, new OutHandler {
          override def onPull() = pull(in)
        })
      }
    }

    def serverLogic (conn: Tcp.IncomingConnection)(implicit system: ActorSystem): Flow[ByteString, ByteString, NotUsed] =
      Flow.fromGraph(GraphDSL.create() { implicit b ⇒
      import GraphDSL.Implicits._
      val welcome = Source.single(ByteString(s"Welcome port ${conn.remoteAddress}!\n"))
      val logic = b.add(Flow[ByteString]
        .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 256, allowTruncation = true))
        .map(_.utf8String)
        .map { msg ⇒ system.log.debug(s"Server received: $msg"); msg }
        .via(closeConnection).via(mqttFlowStage).map(ByteString(_)))

      val concat = b.add(Concat[ByteString]())

      welcome ~> concat.in(0)
      logic.outlet ~> concat.in(1)

      FlowShape(logic.in, concat.out)
    })

    val connectionHandler: Sink[Tcp.IncomingConnection, Future[Done]] =
      Sink.foreach[Tcp.IncomingConnection] { conn =>
        println(s"Incoming connection from: ${conn.remoteAddress}")
        conn.handleWith(serverLogic(conn))
      }

    val incomingCnnections: Source[Tcp.IncomingConnection, Future[Tcp.ServerBinding]] =
      Tcp().bind(address, port)

    val binding: Future[Tcp.ServerBinding] =
      incomingCnnections.to(connectionHandler).run()

    binding onComplete {
      case Success(b) =>
        println(s"Server started, listening on: ${b.localAddress}")
      case Failure(e) =>
        println(s"Server could not be bound to $address:$port: ${e.getMessage}")
    }
  }

  def client(system: ActorSystem, address: String, port: Int): Unit = {
    implicit val sys = system
    import system.dispatcher
    implicit val materializer = ActorMaterializer()

    val testInput = ('a' to 'z').map(ByteString(_))

    val result = Source(testInput).via(Tcp().outgoingConnection(address, port)).
      runFold(ByteString.empty) { (acc, in) ⇒ acc ++ in }

    result.onComplete {
      case Success(successResult) =>
        println(s"Result: " + successResult.utf8String)
        println("Shutting down client")
        system.terminate()
      case Failure(e) =>
        println("Failure: " + e.getMessage)
        system.terminate()
    }
  }
}
