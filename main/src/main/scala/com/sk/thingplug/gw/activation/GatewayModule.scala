package com.sk.thingplug.gw.activation

import java.io.File

import akka.actor.ActorSystem
import akka.osgi.ActorSystemActivator
import com.sk.thingplug._
import com.sk.thingplug.api.{ActorComponent, ServiceComponent}
import com.sk.thingplug.gw.GatewayActor.{BootUp, BootUpService}
import com.sk.thingplug.gw.{Gateway, GatewayMainContext}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import org.osgi.framework.{BundleContext, ServiceReference, ServiceRegistration}
import org.osgi.service.cm.ConfigurationAdmin
import org.osgi.util.tracker.{ServiceTracker, ServiceTrackerCustomizer}

import scala.collection.mutable.ListBuffer
import scala.util.control.NonFatal


/**
  * 2017. 6. 8. - Created by Kwon, Yeong Eon
  */
class GatewayModule extends ActorSystemActivator with StrictLogging {


  val services: ListBuffer[ServiceRegistration[_]] = ListBuffer()
  var config:Config = ConfigFactory.empty
  var context:BundleContext = _
  var actorTracker:ServiceTracker[ActorComponent,ActorComponent] = _
  var mainContext:MainContext = _

  // TODO check need singleton materializer?
//  implicit val m = ActorMaterializer()

  def configure(context: BundleContext, system: ActorSystem) {
    registerActorSystem(context, system)
//    bind[ActorSystem].toInstance(system)
//    bind[ActorMaterializer].toInstance(m)
//    //bindActorComponentInstance(GatewayActor)
//    bind[MainContext].to[GatewayMainContext].in[Singleton]
//    bind[RootServiceComponent].to
    Gateway.banner()
    mainContext = new GatewayMainContext(system)
  }
  override def getActorSystemName(context: BundleContext): String = "TPGW"

  override def getActorSystemConfiguration(context: BundleContext): Config = config

  override def start(context: BundleContext): Unit = {
    this.context = context
    actorTracker = new ServiceTracker(context, classOf[ActorComponent], new GatewayRootActorCustomizer())
    // TODO use ServiceTracker??

    val ref = context.getServiceReference(classOf[ConfigurationAdmin])
    val configAdmin = context.getService(ref)
    try {
      val tmpconfig = configAdmin.getConfiguration("gateway").getProperties().get("config.file")
      config = ConfigFactory.parseFile(new File(tmpconfig.toString))
    } catch {
      case e:NullPointerException => println("gateway no config.file found")
      case NonFatal(e) => println(e)
    }

    super.start(context)
  }

  override def stop(context: BundleContext) {
    unregisterServices(context)
    super.stop(context)
  }

  def unregisterServices(context: BundleContext) {
    services foreach (_.unregister())
  }

  def registerActorSystem(context: BundleContext, system: ActorSystem): Unit = {
    services += context.registerService(classOf[ActorSystem], system, null)
  }

//  override def name: String = MainContext.rootModuleName
//
//  override def stop(): Unit = {
//    //system.terminate()
//    Await.ready(system.terminate(), 2.seconds)
//    super.stop()
//  }
  class GatewayRootActorCustomizer extends ServiceTrackerCustomizer[ActorComponent,ActorComponent] {
    override def addingService(reference: ServiceReference[ActorComponent]) = {
      val actorComponent:ActorComponent = context.getService(reference)
      // TODO logging
      println(s"addingservice ActorComponent ${actorComponent.name}")
      mainContext.rootActor ! BootUp(Set(actorComponent))
      actorComponent
    }

    override def removedService(reference: ServiceReference[ActorComponent], service: ActorComponent) = {
      println(s"removedService ActorComponent ${service.name}")
      context.ungetService(reference)
    }

    override def modifiedService(reference: ServiceReference[ActorComponent], service: ActorComponent) = {
      println(s"modifiedService ActorComponent ???? ${service.name}")
    }
  }
  class GatewayServiceComponentCustomizer extends ServiceTrackerCustomizer[ServiceComponent,ServiceComponent] {
    override def addingService(reference: ServiceReference[ServiceComponent]): ServiceComponent = {
      val service = context.getService(reference)
      // TODO logging
      println(s"addingService ServiceComponent ${service.name}")
      mainContext.rootActor ! BootUpService(service)
      service
    }

    override def removedService(reference: ServiceReference[ServiceComponent], service: ServiceComponent): Unit = {
      println(s"removedService ActorComponent ${service.name}")
      context.ungetService(reference)
    }

    override def modifiedService(reference: ServiceReference[ServiceComponent], service: ServiceComponent): Unit = {
      println(s"modifiedService ActorComponent ???? ${service.name}")
    }
  }

}
