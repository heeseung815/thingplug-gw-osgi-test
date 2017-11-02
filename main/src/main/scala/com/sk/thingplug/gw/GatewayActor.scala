package com.sk.thingplug.gw

import java.net.BindException

import akka.actor.SupervisorStrategy.{Restart, Resume, Stop}
import akka.actor.{Actor, OneForOneStrategy, Props, SupervisorStrategy}
import com.sk.thingplug.api.{ActorComponent, ServiceComponent}
import com.sk.thingplug.MainContext
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._
import scala.language.postfixOps


/**
  * 2017. 6. 7. - Created by Kwon, Yeong Eon
  */
class GatewayActor extends Actor with StrictLogging {
  import GatewayActor._
  var services:Set[ServiceComponent] = Set.empty[ServiceComponent]

  override val supervisorStrategy: SupervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
    case _: ArithmeticException =>
      Resume
    case _: NullPointerException =>
      Restart
    case _: IllegalArgumentException =>
      Stop
    case x: BindException =>
      logger.error(s"Child Actor bind error", x)
      Stop
    case x: Exception =>
      logger.error(s"Child Actor failed", x)
      Restart
  }

  override def preStart(): Unit = {
    super.preStart()
  }

  override def receive: Receive = {
    case BootUp(actors) =>
      for(actor <- actors) {
        val ref = context.actorOf(actor.actorProps(), actor.name)
        //mainContext.registerNamedActor(actor.name, ref)
      }
    case BootUpService(service) =>
      services += service
      service.start()
    case Shutdown =>
      stopServices()
      context.stop(self)
      logger.info("stoped GatewayActor")
  }
  def stopServices(): Unit = {
    services.foldRight(0)(
      (service,i) => {
        service.stop()
        logger.info(s"service: ${service.name} stopped")
        i+1
      }
    )
    //    for(service <- services) {
    //      logger.info(s"stopping service ${service.name}")
    //      service.stop()
    //    }
  }
}

object GatewayActor extends ActorComponent {
  override def name: String = MainContext.rootActorName
  override def actorProps(): Props = Props(classOf[GatewayActor])
  case class BootUp(actors:Set[ActorComponent])
  case class BootUpService(service:ServiceComponent)
  case object Shutdown
  case object BootUpAck
}
