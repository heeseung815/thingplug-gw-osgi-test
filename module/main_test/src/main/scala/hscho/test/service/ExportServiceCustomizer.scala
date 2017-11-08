package hscho.test.service

import com.sk.thingplug.api.ExportService
import com.typesafe.scalalogging.StrictLogging
import org.osgi.framework.{BundleContext, ServiceReference}
import org.osgi.util.tracker.ServiceTrackerCustomizer

/**
  * Created by kylee on 2017. 9. 13..
  */
class ExportServiceCustomizer(context:BundleContext, builder: RunnableGraphBuilder) extends ServiceTrackerCustomizer[ExportService[Any, Any, Any, Any], Any]
  with StrictLogging {
  override def addingService(reference: ServiceReference[ExportService[Any, Any, Any, Any]]): AnyRef = {
    logger.info(s"ExportServiceCustomizer.addingService: reference=${reference.toString}")
    val service = context.getService(reference)
    if (service != null) {
      builder.reloadExportService(service)
    } else {
      logger.error(s"ExportServiceCustomizer.addingService: Fail to find service. reference=${reference.toString}")
    }
    service
  }

  override def removedService(reference: ServiceReference[ExportService[Any, Any, Any, Any]], service: Any): Unit = {
    logger.info(s"ExportServiceCustomizer.removedService : ${reference.toString}")
    if (service != null)
      builder.removeExportService(service.asInstanceOf[ExportService[Any, Any, Any, Any]])
  }

  override def modifiedService(reference: ServiceReference[ExportService[Any, Any, Any, Any]], service: Any): Unit = {
    logger.info(s"ExportServiceCustomizer.modifiedService : ${reference.toString}")
    if (service != null)
      builder.reloadExportService(service.asInstanceOf[ExportService[Any, Any, Any, Any]])
  }
}
