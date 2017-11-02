package com.sk.thingplug.api

import akka.stream.scaladsl.Source

/**
  * Created by kylee on 2017. 9. 5..
  */
trait GraphSource[+Out, +Mat, Setting] {
  def source(setting:Setting):Source[Out, Mat]
  def broadcastHubSource(setting:Setting):Source[Out, Mat]
}
