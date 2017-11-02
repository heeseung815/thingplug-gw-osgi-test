package com.sk.thingplug.gw.flow.preprocessing_current.activation

import com.sk.thingplug.api.FlowService
import com.typesafe.scalalogging.StrictLogging
import org.osgi.framework.{BundleActivator, BundleContext, ServiceRegistration}

/**
  * Created by kylee on 2017. 9. 20..
  */
class PreProcessingCurrentActivator extends BundleActivator with StrictLogging {
  var registration :ServiceRegistration[_] = null

  override def start(context: BundleContext): Unit = {
    logger.info(s"RuleEngineActivator.start: context=${context.toString}")
    registration = context.registerService(classOf[FlowService[Any, Any, Any, Any]].getName, new PreProcessingCurrentServiceFactory(context), null)
  }

  override def stop(context: BundleContext): Unit = {
    logger.info(s"RuleEngineActivator.stop: context=${context.toString}")
    registration.unregister()
  }
}
