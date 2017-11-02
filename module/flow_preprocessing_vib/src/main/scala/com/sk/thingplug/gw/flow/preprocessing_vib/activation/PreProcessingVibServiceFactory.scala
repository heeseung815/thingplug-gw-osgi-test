package com.sk.thingplug.gw.flow.preprocessing_vib.activation

import com.typesafe.scalalogging.StrictLogging
import org.osgi.framework.{Bundle, BundleContext, ServiceFactory, ServiceRegistration}

/**
  * Created by kylee on 2017. 9. 20..
  */
class PreProcessingVibServiceFactory(context:BundleContext) extends ServiceFactory[PreProcessingVibService] with StrictLogging {
  override def getService(bundle: Bundle, registration: ServiceRegistration[PreProcessingVibService]): PreProcessingVibService = {
    logger.info(s"PreProcessingVibServiceFactory.getService: context=${context.getBundle.getSymbolicName}, bundle=${bundle.getSymbolicName}, className=${bundle.getClass.getName}")
    val service = new PreProcessingVibService(context.getBundle.getSymbolicName, 100)
    service
  }

  override def ungetService(bundle: Bundle, registration: ServiceRegistration[PreProcessingVibService], service: PreProcessingVibService): Unit = {
    logger.info(s"PreProcessingVibServiceFactory.ungetService")
  }
}
