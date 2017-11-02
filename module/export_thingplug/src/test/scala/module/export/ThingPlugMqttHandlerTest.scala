package module.export

import akka.actor.{ActorRef, ActorSystem}
import com.sk.thingplug.MainContext
import com.sk.thingplug.api.{GraphData, GraphMessageType}
import com.sk.thingplug.gw.export.thingplug.ThingPlugActor
import com.sk.thingplug.gw.export.thingplug.ThingPlugActor.{StatisticTick, Telemetry}
import com.sk.thingplug.gw.export.thingplug.mqtt.ThingPlugMqttHandler
import com.typesafe.scalalogging.StrictLogging

object ThingPlugMqttHandlerTest extends App with StrictLogging {
  import scala.concurrent.ExecutionContext.Implicits.global
  val h = new ThingPlugMqttHandler()
  h.init( null)
  for {
    i <- 0 to 100
  } {
    h.onDeviceTelemetry(GraphData(s"deviceName$i", "0070341000000008", GraphMessageType.Telemetry, Seq(Map.empty)))
  }
  Thread.sleep(10000)
}

object ThingPlugActorTest extends App with StrictLogging {
  val system = ActorSystem("test")
  /*
  val actor = system.actorOf(ThingPlugActor.actorProps(new MainContext {override def namedActor(name: String): Option[ActorRef] = ???

    override def unregisterNamedActor(name: String): Option[ActorRef] = ???

    override def instanceOf[T](clazz: Class[T]): T = ???

    override def registerNamedActor(name: String, actorRef: ActorRef): Unit = logger.info("register")

    override val rootActor: ActorRef = null
    override val actorSystem: ActorSystem = system
  }))
  for {
    i <- 0 to 1000
  } {
    actor !  Telemetry(GraphData(s"deviceName$i", "0070341000000008", Seq(Map("a"->"a"))))

  }
  Thread.sleep(5000)
  actor ! StatisticTick
  Thread.sleep(10000)
  */
}