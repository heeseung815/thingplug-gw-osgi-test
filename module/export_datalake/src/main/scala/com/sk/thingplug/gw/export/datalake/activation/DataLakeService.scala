package com.sk.thingplug.gw.export.datalake.activation

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, MergeHub, Sink}
import com.sk.thingplug.api.{ExportService, GraphData}
import com.sk.thingplug.gw.export.datalake.DataLakeActor
import com.typesafe.config.Config

class DataLakeService(bname: String)(implicit val actorSystem: ActorSystem, val materializer: ActorMaterializer) extends ExportService[GraphData, DataLakeActor.Telemetry, Any, Config] {
  override def bundleName: String = bname
  override def className: String = classOf[DataLakeService].getName

  val sinkActorRef = actorSystem.actorOf(Props(new DataLakeActor))

  override def sink(setting: Config): Sink[GraphData, Any] = flow.to(Sink.actorRefWithAck(sinkActorRef, DataLakeActor.Init, DataLakeActor.Ack, DataLakeActor.Complete)).named("DataLakeGraph.sink")

  override def mergeHubSink(setting: Config): Sink[GraphData, Any] = MergeHub.source[GraphData](perProducerBufferSize = 128).to(sink(setting)).run()(materializer).named("DataLakeGraph.mergeHubSink")

  override def flow: Flow[GraphData, DataLakeActor.Telemetry, Any] = Flow[GraphData].map(data => DataLakeActor.Telemetry(data)).named("DataLakeGraph.flow")
}
