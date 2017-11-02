package com.sk.thingplug.gw.service

import com.sk.thingplug.api.ImportService
import com.typesafe.scalalogging.StrictLogging
import org.osgi.framework.{BundleContext, ServiceReference}
import org.osgi.util.tracker.ServiceTrackerCustomizer

/**
  * Created by kylee on 2017. 9. 13..
  */
class ImportServiceCustomizer(context:BundleContext, builder: RunnableGraphBuilder) extends ServiceTrackerCustomizer[ImportService[Any, Any, Any, Any], Any]
  with StrictLogging {
  override def addingService(reference: ServiceReference[ImportService[Any, Any, Any, Any]]): AnyRef = {
    logger.info(s"ImportServiceCustomizer.addingService : ${reference.toString}")
    val service = context.getService(reference)
    if (service != null) {
      builder.reloadImportService(service)
    } else {
      logger.error(s"ImportServiceCustomizer.addingService: Fail to find service. reference=${reference.toString}")
    }
    service
  }

  override def removedService(reference: ServiceReference[ImportService[Any, Any, Any, Any]], service: Any): Unit = {
    logger.info(s"ImportServiceCustomizer.removedService : ${reference.toString}")
    if (service != null) {
      builder.removeImportService(service.asInstanceOf[ImportService[Any, Any, Any, Any]])
    } else {
      logger.error(s"ImportServiceCustomizer.removedService: Fail to find service. reference=${reference.toString}")
    }
    logger.info(s"ImportServiceCustomizer.removedService : Done")
  }

  override def modifiedService(reference: ServiceReference[ImportService[Any, Any, Any, Any]], service: Any): Unit = {
    logger.info(s"ImportServiceCustomizer.modifiedService : ${reference.toString}")
    if (service != null) {
      builder.reloadImportService(service.asInstanceOf[ImportService[Any, Any, Any, Any]])
    } else {
      logger.error(s"ImportServiceCustomizer.modifiedService: Fail to find service. reference=${reference.toString}")
    }
  }
}
