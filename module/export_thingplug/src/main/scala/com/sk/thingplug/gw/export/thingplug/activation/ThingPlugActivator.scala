package com.sk.thingplug.gw.export.thingplug.activation

import akka.actor.ActorSystem
import akka.osgi.ActorSystemActivator
import akka.stream.ActorMaterializer
import com.sk.thingplug.api.{ActorComponent, BidExportService, GraphData}
import com.sk.thingplug.gw.export.thingplug.ThingPlugActor
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import org.osgi.framework.{BundleActivator, BundleContext}

class ThingPlugActivator extends ActorSystemActivator with StrictLogging{
  override def stop(context: BundleContext) = {
    logger.info(s"ThingPlugActivator.stop: ${context.toString}")
  }

  override def configure(context: BundleContext, system: ActorSystem): Unit = {
    logger.info(s"ThingPlugActivator.configure: ${system.toString}")
    implicit val materializer:ActorMaterializer = ActorMaterializer.create(system)
    implicit val s:ActorSystem = system

    context.registerService(classOf[ActorComponent], ThingPlugActor, null)
    context.registerService(classOf[BidExportService[Any, Any, Any, Any, Any, Any]].getName, new ThingPlugServiceFactory(context), null)
  }
}
