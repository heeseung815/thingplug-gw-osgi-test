package com.sk.thingplug.gw

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.{ask, gracefulStop}
import akka.util.Timeout
import com.sk.thingplug.api.{ActorComponent, ServiceComponent}

import scala.concurrent.duration._
import com.sk.thingplug.{MainContext, RootServiceComponent}
import com.typesafe.scalalogging.StrictLogging

import scala.collection.concurrent.TrieMap
import scala.collection.immutable.Set
import scala.collection.mutable
import scala.concurrent.Await
import scala.language.postfixOps

class GatewayRootService (
                                    services: Set[ServiceComponent],
                                    actors: Set[ActorComponent],
                                    mainContext: MainContext,
//                                    val injector: Injector,
                                    val actorSystem: ActorSystem
                                  )
  extends RootServiceComponent with StrictLogging{

  //var services: Set[ServiceComponent] = _

  override def name: Option[String] = Some(MainContext.rootServiceName)

  override def start(): Unit = init()

  override def stop(): Unit = shutdown()


  def startActors(): Unit = {
    implicit val timeout = Timeout(2 seconds)
    val f = mainContext.rootActor ? GatewayActor.BootUp(actors)
    Await.ready(f, 3 seconds)
    logger.debug(s"startActors $f")
  }
  def stopActors(): Unit = {
    //rootActor ! GatewayActor.Shutdown
    Await.ready(gracefulStop(mainContext.rootActor, 2 seconds), 3 seconds)
  }
  def startServices(): Unit = {
    logger.info("startServices")
    services.foldLeft(0)(
      (i,service) => {
        logger.info(s"service: ${service.name} starting")
        service.start()
        i+1
      }
    )
//    for(service <- services) {
//      logger.info(s"starting service ${service.name}")
//      service.start()
//    }
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
  logger.debug("\n\n---------> GatewayRootService\n\n")

  def init(): Unit = {
    logger.info("GatewayRootService starting")

    startActors()
    /* SET 처리 관련. TO DO */
    //import net.codingwell.scalaguice.InjectorExtensions._
    //services = injector.instance[Set[ServiceComponent]]
    //services = Set(injector.getInstance(classOf[LoraConsumerService]))

    startServices()
  }

  def shutdown(): Unit = {
    stopServices()
    stopActors()
    logger.info("GatewayRootService stopped")
  }
}
