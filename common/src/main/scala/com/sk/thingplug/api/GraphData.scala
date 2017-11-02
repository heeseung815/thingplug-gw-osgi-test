package com.sk.thingplug.api

/**
  * Created by kylee on 2017. 9. 7..
  */
object GraphMessageType {
  object Connect
  object Disconnect
  object Telemetry
  object Attributes
  object WriteAttribute
  object Command
}

case class GraphData(var deviceName:String, var serviceId:String, messageType: Any, var message:Seq[Map[String, Any]])
