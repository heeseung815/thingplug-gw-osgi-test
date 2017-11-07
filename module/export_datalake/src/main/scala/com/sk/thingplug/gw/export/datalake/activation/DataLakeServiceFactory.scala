package com.sk.thingplug.gw.export.datalake.activation

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.osgi.framework.{Bundle, BundleContext, ServiceFactory, ServiceRegistration}

class DataLakeServiceFactory(context: BundleContext)(implicit val actorSystem: ActorSystem, val materializer: ActorMaterializer) extends ServiceFactory[DataLakeService] {
  override def getService(bundle: Bundle, registration: ServiceRegistration[DataLakeService]): DataLakeService = {
    val service = new DataLakeService(context.getBundle.getSymbolicName)
    println(s"DataLakeServiceFactory.getService: context=${context.getBundle.getSymbolicName}, className=${service.className}")
    service
  }

  override def ungetService(bundle: Bundle, registration: ServiceRegistration[DataLakeService], service: DataLakeService): Unit = {

  }
}
