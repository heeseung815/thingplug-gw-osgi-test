package com.sk.thingplug.gw.export.thingplug

import akka.actor.{Actor, Props, Timers}
import com.sk.thingplug.gw.export.thingplug.mqtt.ThingPlugMqttHandler

import scala.concurrent.Future
import com.sk.thingplug.api.{ActorComponent, GraphData}
import com.sk.thingplug.gw.GatewayConfig
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._
import scala.util.{Failure, Success}


trait ThingPlugConnection {
  def init(connectionLostUserFunc:Throwable => Unit): Future[Any]
  def onDeviceTelemetry(data:GraphData): Future[Any]
  def onDeviceAttribute(data:GraphData):Future[Any]
  def onDeviceConnect(data:GraphData):Future[Any]
  def onDeviceDisconnect(data:GraphData):Future[Any]
  def onAttribute(message:Any):Future[Any]
  def onRpc(message:Any):Future[Any]
  def onTelemetryTick():Unit
  def onStatisticTick():Unit
}

object ThingPlugActor extends ActorComponent {

  // TODO config
  override def name: String = "thingplug"
  override def actorProps(): Props = Props(classOf[ThingPlugActor])

  case object Init
  case object Ack
  case object Complete
  case object DisconnectTickKey
  case object DisconnectTick
  case object StatisticTickKey
  case object StatisticTick
  case object ReconnectTickKey
  case object ReconnectTick
  case class Telemetry(data:GraphData)
  case class Attribute(data:GraphData)
  case class DeviceConnect(data:GraphData)
  case class DeviceDisconnect(data:GraphData)
  case class Rpc(/*TODO message format*/)
  // TODO add other message definition
}
class ThingPlugActor extends Actor with StrictLogging with Timers {
  import ThingPlugActor._
  import scala.concurrent.ExecutionContext.Implicits.global

  val connectionHandler:ThingPlugConnection = ThingPlugMqttHandler()

  val config: Config = GatewayConfig.config
  val disconnectTimer: Int = if (config.hasPath("thingplug.export.thingplug.timer.disconnect")) config.getInt("thingplug.export.thingplug.timer.disconnect") else 3600000
  val statisticTimer: Int = if (config.hasPath("thingplug.export.thingplug.timer.statistic")) config.getInt("thingplug.export.thingplug.timer.statistic") else 3600000

  timers.startPeriodicTimer(DisconnectTickKey, DisconnectTick, disconnectTimer.milliseconds )
  timers.startPeriodicTimer(StatisticTickKey, StatisticTick,  statisticTimer.milliseconds)

  val result = connectionHandler.init(connectionLost)
  result.onComplete{
    case Success(a) => logger.info("ThingPlugActor mqtt connections initialized.")
    case Failure(e) => {
      timers.startPeriodicTimer(ReconnectTickKey, ReconnectTick,  1000.milliseconds)
    }
  }

  override def receive: Receive = {
    case Init =>
      sender ! Ack
    case Telemetry(data:GraphData) =>
      val result = connectionHandler.onDeviceTelemetry(data)
      val orginSender = sender()
      result.onComplete( _ => orginSender ! Ack)
    case msg : Attribute =>
      // TODO publish attribute msg to mqtt attribute topic
      sender ! Ack
    case DeviceConnect(data:GraphData) =>
      val result = connectionHandler.onDeviceConnect(data)
      val orginSender = sender()
      result.onComplete( _ => orginSender ! Ack)
    case DeviceDisconnect(data:GraphData) =>
      val result = connectionHandler.onDeviceDisconnect(data)
      val orginSender = sender()
      result.onComplete( _ => orginSender ! Ack)
    case msg : Rpc =>
      // TODO
      sender ! Ack
    case DisconnectTick =>
      logger.info(s"TelemetryTick : ${System.currentTimeMillis()}")
      connectionHandler.onTelemetryTick()
    case StatisticTick =>
      logger.info(s"StatisticTick : ${System.currentTimeMillis()}")
      connectionHandler.onStatisticTick()
    case ReconnectTick =>
      logger.info(s"ReconnectTick : ${System.currentTimeMillis()}")
      val result = connectionHandler.init(connectionLost)
      result.onComplete{
        case Success(a) => timers.cancel(ReconnectTickKey)
        case Failure(e) => timers.startPeriodicTimer(ReconnectTickKey, ReconnectTick,  1000.milliseconds)
      }
  }

  def connectionLost(cause:Throwable) : Unit = {
    timers.startPeriodicTimer(ReconnectTickKey, ReconnectTick,  1000.milliseconds)
  }
}


