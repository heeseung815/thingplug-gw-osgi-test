package com.sk.thingplug.gw.activation

import akka.actor.ActorSystem
import akka.osgi.ActorSystemActivator
import akka.stream.ActorMaterializer
import com.sk.thingplug.api._
import com.sk.thingplug.gw.Gateway
import com.sk.thingplug.gw.service.{RunnableGraphBuilder, _}
import org.osgi.framework.{BundleActivator, BundleContext}
import org.osgi.util.tracker.ServiceTracker

/**
  * Created by kylee on 2017. 9. 13..
  */
class GraphActivator extends ActorSystemActivator {
  var importServiceTracker:ServiceTracker[ImportService[Any, Any, Any, Any], Any] = _
  var bidExportServiceTracker:ServiceTracker[BidExportService[Any, Any, Any, Any, Any, Any], Any] = _
  var flowServiceTracker:ServiceTracker[FlowService[Any, Any, Any, Any], Any] = _

  var graphBuilder:RunnableGraphBuilder = _

  override def stop(context: BundleContext): Unit = {
    importServiceTracker.close()
    bidExportServiceTracker.close()
    flowServiceTracker.close()
  }

  override def configure(context: BundleContext, system: ActorSystem): Unit = {
    implicit val materializer:ActorMaterializer = ActorMaterializer.create(system)
    implicit val s:ActorSystem = system

    println(s"GraphActivator.configure: ${system.toString}")
    graphBuilder = new RunnableGraphBuilder(context,
      GraphConfig(
        Seq(
          RunnableGraphConfig(
            "com.sk.thingplug.gw.deviceimport.lora.activation.LoraService",
            Seq("com.sk.thingplug.gw.flow.preprocessing_vib.activation.PreProcessingVibService"),
            "com.sk.thingplug.gw.export.thingplug.activation.ThingPlugService"
          )
          /*
          ,
          RunnableGraphConfig(
            "com.sk.thingplug.gw.deviceimport.lora.activation.LoraService",
            "com.sk.thingplug.gw.flow.preprocessing_current.activation.PreProcessingCurrentService",
            "com.sk.thingplug.gw.export.thingplug.activation.ThingPlugService"
          )
          */
        )
      ))
    importServiceTracker = new ServiceTracker(context, classOf[ImportService[Any, Any, Any, Any]].getName,
      new ImportServiceCustomizer(context, graphBuilder))
    importServiceTracker.open()

    bidExportServiceTracker = new ServiceTracker(context, classOf[BidExportService[Any, Any, Any, Any, Any, Any]].getName,
      new BidExportServiceCustomizer(context, graphBuilder))
    bidExportServiceTracker.open()

    flowServiceTracker = new ServiceTracker(context, classOf[FlowService[Any, Any, Any, Any]].getName,
      new FlowServiceCustomizer(context, graphBuilder))
    flowServiceTracker.open()

    //graphBuilder.reload()

    Gateway.banner()
  }
}
