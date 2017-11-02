package com.sk.thingplug.gw.flow.preprocessing_current.activation

import akka.NotUsed
import akka.stream.{FlowShape, Graph}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge}
import com.sk.thingplug.api.{FlowService, GraphData}

/**
  * Created by kylee on 2017. 9. 8..
  */
class PreProcessingCurrentService(bname: String, startTime: Long) extends FlowService[GraphData, GraphData, Any, Any]{
  override def flow: Flow[GraphData, GraphData, Any] = createPartialGraph

  override def className: String = classOf[PreProcessingCurrentService].getName

  override def bundleName: String = bname

  def createPartialGraph: Flow[GraphData, GraphData, NotUsed] = {
    val partial: Graph[FlowShape[GraphData, GraphData], NotUsed] = GraphDSL.create() { implicit builder =>
      import akka.stream.scaladsl.GraphDSL.Implicits._

      val broad = builder.add(Broadcast[GraphData](2))
      val proc1 = builder.add(new PreProcessingCurrentFlow(startTime))
      val proc2 = builder.add(new PreProcessingCurrentFlow(startTime))
      val merge = builder.add(Merge[GraphData](2))

      broad ~> proc1 ~> merge.in(0)
      broad ~> proc2 ~> merge.in(1)

      FlowShape(broad.in, merge.out)
    }

    Flow.fromGraph(partial)
  }
}
