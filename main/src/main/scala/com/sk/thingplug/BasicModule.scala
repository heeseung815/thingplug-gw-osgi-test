package com.sk.thingplug

//import com.google.inject.AbstractModule
//import com.google.inject.name.Names
//import net.codingwell.scalaguice.{ScalaModule, ScalaMultibinder}

/**
  * Created by kwonkr01 on 2017-06-19.
  */
//abstract class BasicModule extends AbstractModule with CoreModule with ScalaModule {
//  def bindServiceClass[T <: ServiceComponent : Manifest]: Unit = {
//    val serviceBinder = ScalaMultibinder.newSetBinder[ServiceComponent](binder)
//    serviceBinder.addBinding.to[T]
//  }
//  def bindServiceClassWithName[T <: ServiceComponent : Manifest](name:String): Unit = {
//    val serviceBinder = ScalaMultibinder.newSetBinder[ServiceComponent](binder/*, Names.named(name)*/)
//    serviceBinder.addBinding.to[T]
//  }
//  def bindActorComponentInstance(instance:ActorComponent): Unit = {
//    val actorBinder = ScalaMultibinder.newSetBinder[ActorComponent](binder)
//    //ScalaMultibinder.newSetBinder[ActorComponent](binder/*, Names.named(instance.name.get)*/)
//    actorBinder.addBinding.toInstance(instance)
//  }
//}