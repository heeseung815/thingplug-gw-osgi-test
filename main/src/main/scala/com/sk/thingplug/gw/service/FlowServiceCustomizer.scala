package com.sk.thingplug.gw.service

import com.sk.thingplug.api.FlowService
import com.typesafe.scalalogging.StrictLogging
import org.osgi.framework.{BundleContext, ServiceReference}
import org.osgi.util.tracker.ServiceTrackerCustomizer

/**
  * Created by kylee on 2017. 9. 20..
  */
class FlowServiceCustomizer (context:BundleContext, builder: RunnableGraphBuilder) extends ServiceTrackerCustomizer[FlowService[Any, Any, Any, Any], Any] with StrictLogging {
  override def addingService(reference: ServiceReference[FlowService[Any, Any, Any, Any]]): AnyRef = {
    logger.info(s"FlowServiceCustomizer.addingService : ${reference.toString}")
    val service = context.getService(reference)
    if (service != null) {
      builder.reloadFlowService(service)
    } else {
      logger.error(s"FlowServiceCustomizer.addingService: Fail to find service. reference=${reference.toString}")
    }
    service
  }

  override def removedService(reference: ServiceReference[FlowService[Any, Any, Any, Any]], service: Any): Unit = {
    logger.info(s"FlowServiceCustomizer.removedService : ${reference.toString}")
    if (service != null) {
      builder.removeFlowService(service.asInstanceOf[FlowService[Any, Any, Any, Any]])
    } else {
      logger.error(s"FlowServiceCustomizer.removedService: Fail to find service. reference=${reference.toString}")
    }
  }

  override def modifiedService(reference: ServiceReference[FlowService[Any, Any, Any, Any]], service: Any): Unit = {
    logger.info(s"FlowServiceCustomizer.modifiedService : ${reference.toString}")
    if (service != null) {
      builder.reloadFlowService(service.asInstanceOf[FlowService[Any, Any, Any, Any]])
    } else {
      logger.error(s"FlowServiceCustomizer.modifiedService: Fail to find service. reference=${reference.toString}")
    }
  }
}
