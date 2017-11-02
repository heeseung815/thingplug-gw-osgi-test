package com.sk.thingplug

import akka.actor.{Actor, ActorRef, ActorSystem}
import com.typesafe.scalalogging.StrictLogging

/**
  * Created by kwonkr01 on 2017-06-16.
  */
trait MainContext {
  val actorSystem: ActorSystem

  val rootActor: ActorRef

  def registerNamedActor(name: String, actorRef: ActorRef): Unit

  def unregisterNamedActor(name: String): Option[ActorRef]

  def namedActor(name: String): Option[ActorRef]

  def instanceOf[T](clazz: Class[T]): T
}
abstract class MainContextAwareActor(mainContext: MainContext) extends Actor with StrictLogging {
  override def preStart(): Unit = {
    super.preStart()
    registerMainContext()
    logger.info(s"actor: ${self.path.name} started")
  }

  def registerMainContext(): Unit = {
    mainContext.registerNamedActor(self.path.name, self)
  }
  def unregisterMainContext(): Option[ActorRef] = {
    mainContext.unregisterNamedActor(self.path.name)
  }

  def preStartAfterRegister() : Unit = ()

  override def postStop(): Unit = {
    logger.info(s"actor: ${self.path.name} stopping")
    unregisterMainContext()
    super.postStop()
  }
}


object MainContext {

  // module name
  val rootModuleName = "gateway"
  val deviceImportModuleName = "deviceservice"
  val coapModuleName = "coap"
  val tcpModuleName = "tcp"
  val kafkaModuleName = "kafka"
  val ttvKafkaModuleName = "ttvKafka"
  val mqttModuleName = "mqtt" //
  val thingplugModuleName = "thingplug"
  val loraModuleName = "lora"
  val loraSimulationModuleName = "lorasimulation"
  val thingplugActorName = "thingplug"
  val exportServiceModuleName = "thingplug"

  // actor name
  val rootActorName = "gateway"

  // service name
  val rootServiceName = "gateway"
  val deviceServiceName = "deviceservice"
  val gasAMISimulationServiceName = "gasamisimulationservice"
}
