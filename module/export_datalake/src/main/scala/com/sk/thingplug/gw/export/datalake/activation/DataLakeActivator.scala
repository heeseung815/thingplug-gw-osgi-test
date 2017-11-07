package com.sk.thingplug.gw.export.datalake.activation

import akka.actor.ActorSystem
import akka.osgi.ActorSystemActivator
import akka.stream.ActorMaterializer
import com.sk.thingplug.api.ExportService
import com.typesafe.scalalogging.StrictLogging
import org.osgi.framework.BundleContext

class DataLakeActivator extends ActorSystemActivator with StrictLogging {
  override def stop(context: BundleContext): Unit = {
    logger.info(s"DataLakeActivator.stop: ${context.toString}")
  }

  override def configure(context: BundleContext, system: ActorSystem): Unit = {
    logger.info(s"DataLakeActivator.configure: ${system.toString}")
    implicit val materializer: ActorMaterializer = ActorMaterializer.create(system)
    implicit val s: ActorSystem = system

//    context.registerService(classOf[ActorComponent], DataLakeActor, null)
    context.registerService(classOf[ExportService[Any, Any, Any, Any]].getName, new DataLakeServiceFactory(context), null)
  }
}
