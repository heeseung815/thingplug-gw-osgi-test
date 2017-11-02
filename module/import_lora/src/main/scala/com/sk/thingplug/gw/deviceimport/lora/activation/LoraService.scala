package com.sk.thingplug.gw.deviceimport.lora.activation

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.mqtt.scaladsl.{MqttSink, MqttSource}
import akka.stream.alpakka.mqtt.{MqttMessage, MqttSourceSettings}
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Source}
import com.sk.thingplug.api.{GraphData, GraphMessageType, ImportService}
import com.sk.thingplug.gw.deviceimport.lora.{DeviceDataExtractorFromOneM2MNotification, GasAMIDataConverter}

/**
  * Created by kylee on 2017. 9. 12..
  */
class LoraService(bname:String) (implicit val actorSystem:ActorSystem, val materializer:ActorMaterializer) extends ImportService[MqttMessage, GraphData, NotUsed, MqttSourceSettings] {
  override def bundleName: String = bname
  override def className: String = classOf[LoraService].getName

  override def flow: Flow[MqttMessage, GraphData, NotUsed] =
    Flow[MqttMessage].map[(String, String, String)]{ mqttMessage => DeviceDataExtractorFromOneM2MNotification.extract(mqttMessage).asInstanceOf[(String, String, String)] }
      .filter( deviceData => GasAMIDataConverter.validation(deviceData) )
      .map {
        deviceData =>
          GasAMIDataConverter.convert(deviceData)
      }
      .map{ case (serviceId, deviceName, content) => GraphData(deviceName, serviceId, GraphMessageType.Telemetry, Seq(content))}

  override def source(settings: MqttSourceSettings): Source[GraphData, NotUsed] = MqttSource(settings, bufferSize = 128).viaMat(flow)(Keep.right)

  override def broadcastHubSource(settings: MqttSourceSettings): Source[GraphData, NotUsed] = source(settings).toMat(BroadcastHub.sink(bufferSize = 128))(Keep.right).run()(materializer)
}
