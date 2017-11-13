package hscho.test.simpleimport.activation

import akka.actor.ActorSystem
import akka.osgi.ActorSystemActivator
import org.osgi.framework.BundleContext

class ImportTestActivator extends ActorSystemActivator {

  override def stop(context: BundleContext): Unit = {
    println("ImportTestActivator stop.")
  }

  override def configure(context: BundleContext, system: ActorSystem): Unit = {
    println("ImportTestActivator start.")


  }

}
