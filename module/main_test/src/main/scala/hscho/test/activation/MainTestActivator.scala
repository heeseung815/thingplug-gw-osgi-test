package hscho.test.activation

import akka.actor.ActorSystem
import akka.osgi.ActorSystemActivator
import akka.stream.ActorMaterializer
import com.sk.thingplug.api.{ExportService, GraphConfig, RunnableGraphConfig}
import hscho.test.service.{ExportServiceCustomizer, RunnableGraphBuilder}
import org.osgi.framework.BundleContext
import org.osgi.util.tracker.ServiceTracker

class MainTestActivator extends ActorSystemActivator {
  var exportServiceTracker: ServiceTracker[ExportService[Any, Any, Any, Any], Any] = _
  var graphBuilder: RunnableGraphBuilder = _

  override def stop(context: BundleContext): Unit = {
    println("MainTestActivator stop.")
    exportServiceTracker.close()
  }

  override def configure(context: BundleContext, system: ActorSystem): Unit = {
    println("MainTestActivator start.")
    implicit val materializer:ActorMaterializer = ActorMaterializer.create(system)
    implicit val s:ActorSystem = system

    println(s"GraphActivator.configure: ${system.toString}")
    graphBuilder = new RunnableGraphBuilder(context,
      GraphConfig(
        Seq(
          RunnableGraphConfig(
            "",
            Seq(""),
            "com.sk.thingplug.gw.export.datalake.activation.DataLakeService"
          )
        )
      ))
    exportServiceTracker = new ServiceTracker(context, classOf[ExportService[Any, Any, Any, Any]].getName,
      new ExportServiceCustomizer(context, graphBuilder))
    exportServiceTracker.open()

    println("+++ THINGPLUG EDGE +++")
  }

}
