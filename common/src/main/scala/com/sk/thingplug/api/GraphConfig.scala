package com.sk.thingplug.api

import akka.stream.UniqueKillSwitch

/**
  * Created by kylee on 2017. 9. 14..
  */
/*
{
  {
    source = "com.sk.thingplug.gw.deviceimport.lora.activation.LoraService",
    flow = ["com.sk.thingplug.gw.flow.preprocessing_vib.activation.PreProcessingVibService"],
    sink = "com.sk.thingplug.gw.export.thingplug.activation.ThingPlugService"
  }
}
 */

case class GraphConfig(graph:Seq[RunnableGraphConfig])
case class RunnableGraphConfig(source:String, flow:Seq[String], sink:String, var killSwitch:Seq[UniqueKillSwitch]=Seq.empty)