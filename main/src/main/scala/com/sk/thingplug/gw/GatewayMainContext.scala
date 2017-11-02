package com.sk.thingplug.gw

import akka.actor.{ActorRef, ActorSystem}
//import com.google.inject.{Inject, Injector}
import com.sk.thingplug.MainContext
import com.typesafe.scalalogging.StrictLogging

import scala.collection.concurrent.TrieMap

class GatewayMainContext(val actorSystem: ActorSystem) extends MainContext with StrictLogging {
  private val namedActorTable = TrieMap[String, ActorRef]()
  override val rootActor: ActorRef = actorSystem.actorOf(GatewayActor.actorProps(), MainContext.rootActorName)

  override def registerNamedActor(name: String, actorRef: ActorRef): Unit = {
    logger.info(s"registering actor $actorRef $name")
    namedActorTable.put(name, actorRef)
  }

  override def unregisterNamedActor(name: String): Option[ActorRef] = {
    val ret = namedActorTable.remove(name)
    logger.info(s"unregistering actor $name ${ret.getOrElse(null)}")
    ret
  }

  override def namedActor(name: String): Option[ActorRef] = {
    namedActorTable.get(name)
  }

  override def instanceOf[T](clazz: Class[T]): T = {
    //injector.getInstance(clazz)
    ???
  }
}
