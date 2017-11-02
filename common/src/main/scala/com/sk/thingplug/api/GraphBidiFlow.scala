package com.sk.thingplug.api

import akka.stream.scaladsl.{BidiFlow, Flow}

/**
  * Created by kylee on 2017. 9. 5..
  */
trait GraphBidiFlow[-I1, +O1, -I2, +O2, +Mat] {
  def inFlow:Flow[I1, O1, Mat]
  def outFlow:Flow[I2, O2, Mat]
  def bidiFlow:BidiFlow[I1, O1, I2, O2, Mat]
}
