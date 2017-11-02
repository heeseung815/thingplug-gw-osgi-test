package com.sk.thingplug.api

import akka.stream.scaladsl.Sink

/**
  * Created by kylee on 2017. 9. 5..
  */
trait GraphSink[-In, +Mat, Setting] {
  def sink(setting:Setting):Sink[In, Mat]
  def mergeHubSink(setting:Setting):Sink[In, Mat]
}
