package com.sk.thingplug.api

import akka.stream.scaladsl.Flow

/**
  * Created by kylee on 2017. 9. 8..
  */
trait GraphFlow[-In, +Out, +Mat] {
  def flow:Flow[In, Out, Mat]
}
