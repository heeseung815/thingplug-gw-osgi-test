package com.sk.thingplug.gw.flow.preprocessing_vib.activation

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import com.sk.thingplug.api.{FlowService, GraphData}

/**
  * Created by kylee on 2017. 9. 8..
  */
class PreProcessingVibService(bname: String, startTime: Long) extends FlowService[GraphData, GraphData, Any, Any]{
  override def flow: Flow[GraphData, GraphData, Any] = Flow.fromGraph(new PreProcessingVibFlow(startTime))

  override def className: String = classOf[PreProcessingVibService].getName

  override def bundleName: String = bname
}
