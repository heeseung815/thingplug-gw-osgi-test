package com.sk.thingplug.gw.export.thingplug.activation

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{BidiFlow, BroadcastHub, Flow, Keep, MergeHub, Sink, Source}
import com.sk.thingplug.api.{BidExportService, GraphData, GraphMessageType}
import com.sk.thingplug.gw.export.thingplug.ThingPlugActor
import com.typesafe.config.Config

/**
  * Created by kylee on 2017. 9. 12..
  */

class ThingPlugService(bname: String)(implicit val actorSystem:ActorSystem, val materializer:ActorMaterializer) extends BidExportService[ThingPlugActor.Rpc, GraphData, GraphData, ThingPlugActor.Telemetry, Any, Config]{
  override def bundleName: String = bname
  override def className: String = classOf[ThingPlugService].getName

  val sinkActorRef = actorSystem.actorOf(Props(classOf[ThingPlugActor]))

  override def sink(setting:Config): Sink[GraphData, NotUsed] = outFlow.to(Sink.actorRefWithAck(sinkActorRef, ThingPlugActor.Init, ThingPlugActor.Ack, ThingPlugActor.Complete)).named("ThingPlugGraph.sink")

  override def mergeHubSink(setting:Config): Sink[GraphData, NotUsed] = MergeHub.source[GraphData](perProducerBufferSize = 128).to(sink(setting)).run()(materializer).named("ThingPlugGraph.mergeHubSink")

  override def source(setting:Config): Source[GraphData, ActorRef]  = Source.actorRef[ThingPlugActor.Rpc](128, OverflowStrategy.fail).via(inFlow).named("ThingPlugGraph.source")

  override def broadcastHubSource(setting:Config): Source[GraphData, NotUsed] = source(setting).toMat(BroadcastHub.sink(bufferSize = 128))(Keep.right).run()(materializer).named("ThingPlugGraph.broadcastHubSource")

  override def inFlow: Flow[ThingPlugActor.Rpc, GraphData, NotUsed] = Flow[ThingPlugActor.Rpc].map(_ => GraphData(null, null, GraphMessageType.Command, Seq.empty)).named("ThingPlugGraph.inFlow")

  override def outFlow:Flow[GraphData, ThingPlugActor.Telemetry, NotUsed] = Flow[GraphData].map(data => ThingPlugActor.Telemetry(data)).named("ThingPlugGraph.outFlow")

  override def bidiFlow: BidiFlow[ThingPlugActor.Rpc, GraphData, GraphData, ThingPlugActor.Telemetry, NotUsed] = {
    BidiFlow.fromFlows(inFlow, outFlow).named("ThingPlugGraph.bidiFlow")
  }
}
