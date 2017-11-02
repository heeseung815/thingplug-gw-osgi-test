package com.sk.thingplug.gw.flow.preprocessing_current.activation

import com.typesafe.scalalogging.StrictLogging
import org.osgi.framework.{Bundle, BundleContext, ServiceFactory, ServiceRegistration}

/**
  * Created by kylee on 2017. 9. 20..
  */
class PreProcessingCurrentServiceFactory(context:BundleContext) extends ServiceFactory[PreProcessingCurrentService] with StrictLogging {
  override def getService(bundle: Bundle, registration: ServiceRegistration[PreProcessingCurrentService]): PreProcessingCurrentService = {
    logger.info(s"PreProcessingVibServiceFactory.getService: context=${context.getBundle.getSymbolicName}, bundle=${bundle.getSymbolicName}, className=${bundle.getClass.getName}")
    val service = new PreProcessingCurrentService(context.getBundle.getSymbolicName, 100)
    service
  }

  override def ungetService(bundle: Bundle, registration: ServiceRegistration[PreProcessingCurrentService], service: PreProcessingCurrentService): Unit = {
    logger.info(s"PreProcessingVibServiceFactory.ungetService")
  }
}
