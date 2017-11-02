package com.sk.thingplug

/**
  * Created by kwonkr01 on 2017-06-19.
  */
trait CoreModule {
  def name: String
  def start(): Unit = ()
  def stop(): Unit = ()
}
