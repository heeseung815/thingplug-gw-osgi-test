package com.sk.thingplug.gw.export.datalake

import akka.actor.{Actor, ActorSystem, Props}
import com.sk.thingplug.api.{ActorComponent, GraphData}
import com.sk.thingplug.gw.GatewayConfig
import com.sk.thingplug.gw.export.datalake.hdfs.{DataLakeHdfsHandler, JsonFileWriter}
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging

trait DataLakeConnection {
//  def init(): Future[Any]
  def init(): Unit
  def onSensorTelemetryForJson(nodeId: String, data: Map[String, Any]): Unit
  def onSensorConnectForJson(nodeId: String): Option[JsonFileWriter]
  def flushForJson(nodeId: String)
}

object DataLakeActor extends ActorComponent {
//  override def name: String = MainContext.datalakeActorName
//  override def actorProps(mainContext: MainContext): Props = Props(classOf[DataLakeActor], mainContext)
  override def name: String = "datalake"
  override def actorProps(): Props = Props(classOf[DataLakeActor])

  case object Init
  case object Ack
  case object Complete
//  case class Telemetry(nodeId:String, data: Map[String, Any])
  case class Telemetry(data: GraphData)
  case class Timeout(nodeId: String)
}

class DataLakeActor(implicit val system: ActorSystem) extends Actor with StrictLogging {
  import DataLakeActor._

  val handler: DataLakeHdfsHandler = DataLakeHdfsHandler(system.scheduler, self)

  val config: Config = GatewayConfig.config
  val fileFormat: String = if (config.hasPath("thingplug.export.datalake.hdfs.file-format")) config.getString("thingplug.export.datalake.hdfs.file-format") else "json"

  override def receive = {
    //    case Init =>
    //      import scala.concurrent.duration._
    //      logger.info("# Received Init msg.")
    //      val result = Await.result(handler.init(), 1 seconds)
    //      result match {
    //        case e: Throwable => e.printStackTrace(); sender ! Status.Failure
    //        case _ => sender ! Ack
    //      }
    case Init =>
      logger.info("# Received Init msg.")
      handler.init()
      sender ! Ack
    case Telemetry(data) =>
      logger.info(s"# Received Telemetry msg. nodeId=${data.deviceName}, data:${data.message.head}")
      handler.onSensorTelemetryForJson(data.deviceName, data.message.head)
      sender ! Ack
    case Timeout(nodeId) =>
      logger.info(s"# Received Timeout msg. nodeId=$nodeId")
      handler.flushForJson(nodeId)
  }

  override def postStop(): Unit = {
    handler.flushForJson(null)
    super.postStop()
  }
}
