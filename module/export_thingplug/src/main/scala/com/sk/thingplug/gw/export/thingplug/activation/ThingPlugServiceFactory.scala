package com.sk.thingplug.gw.export.thingplug.activation

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import org.osgi.framework.{Bundle, BundleContext, ServiceFactory, ServiceRegistration}

/**
  * Created by kylee on 2017. 9. 12..
  */
class ThingPlugServiceFactory (context:BundleContext)(implicit val actorSystem:ActorSystem, val materializer:ActorMaterializer)extends ServiceFactory[ThingPlugService] {
  override def getService(bundle: Bundle, registration: ServiceRegistration[ThingPlugService]): ThingPlugService = {
    val service = new ThingPlugService(context.getBundle.getSymbolicName)
    println(s"ThingPlugServiceFactory.getService: context=${context.getBundle.getSymbolicName}, className=${service.className}")
    service
  }

  override def ungetService(bundle: Bundle, registration: ServiceRegistration[ThingPlugService], service: ThingPlugService): Unit ={

  }
}
