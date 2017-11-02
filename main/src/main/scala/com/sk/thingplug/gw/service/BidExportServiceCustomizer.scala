package com.sk.thingplug.gw.service

import com.sk.thingplug.api.BidExportService
import com.typesafe.scalalogging.StrictLogging
import org.osgi.framework.{BundleContext, ServiceReference}
import org.osgi.util.tracker.ServiceTrackerCustomizer

/**
  * Created by kylee on 2017. 9. 13..
  */
class BidExportServiceCustomizer(context:BundleContext, builder: RunnableGraphBuilder) extends ServiceTrackerCustomizer[BidExportService[Any, Any, Any, Any, Any, Any], Any]
  with StrictLogging {
  override def addingService(reference: ServiceReference[BidExportService[Any, Any, Any, Any, Any, Any]]): AnyRef = {
    logger.info(s"BidExportServiceCustomizer.addingService: reference=${reference.toString}")
    val service = context.getService(reference)
    if (service != null) {
      builder.reloadBidExportService(service)
    } else {
      logger.error(s"BidExportServiceCustomizer.addingService: Fail to find service. reference=${reference.toString}")
    }
    service
  }

  override def removedService(reference: ServiceReference[BidExportService[Any, Any, Any, Any, Any, Any]], service: Any): Unit = {
    logger.info(s"BidExportServiceCustomizer.removedService : ${reference.toString}")
    if (service != null)
      builder.removeBidExportService(service.asInstanceOf[BidExportService[Any, Any, Any, Any, Any, Any]])
  }

  override def modifiedService(reference: ServiceReference[BidExportService[Any, Any, Any, Any, Any, Any]], service: Any): Unit = {
    logger.info(s"BidExportServiceCustomizer.modifiedService : ${reference.toString}")
    if (service != null)
      builder.reloadBidExportService(service.asInstanceOf[BidExportService[Any, Any, Any, Any, Any, Any]])
  }
}
