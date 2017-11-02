package com.sk.thingplug.gw.stages.flow

import akka.stream.alpakka.mqtt.MqttMessage
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging
import com.uangel.onem2m.resource.lora_v1_0.{ContentInstanceLora, ResourceFactoryLora}

import scala.xml.XML._

class DeviceDataExtractorFromOneM2MNotification[T] extends GraphStage[FlowShape[MqttMessage, (String, String)]] with StrictLogging {
  val in = Inlet[MqttMessage]("DataExtractorFromOnem2mNotification.in")
  val out = Outlet[(String, String)]("DataExtractorFromOnem2mNotification.out")
  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    setHandler(in, new InHandler {
      override def onPush() = grab(in) match {
        case mqttMessage: MqttMessage =>
          if(true) push(out, DeviceDataExtractorFromOneM2MNotification.extract(mqttMessage).asInstanceOf[(String, String)])
          else pull(in)
        case _ => logger.error("Not supported type")
      }
    })
    setHandler(out, new OutHandler {
      override def onPull = pull(in)
    })
  }
}

/* onem2m Notification(XML) parsing example
<m2m:req xmlns:m2m="http://www.onem2m.org/xml/protocols" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <op>1</op>
    <to>bang9211_gateway_subscriber</to>
    <fr>nodeid_bang1</fr>
    <rqi>dbdd7c24-7494-439d-9211-e32485e9665b</rqi>
    <pc>
        <m2m:cin>
            <ri>CI00000000000084949738</ri>
            <ty>4</ty>
            <rn>CI00000000000084949738</rn>
            <pi>CT00000000000000006475</pi>
            <ct>2017-08-02T18:24:39+09:00</ct>
            <lt>2017-08-02T18:24:39+09:00</lt>
            <et>2017-09-01T18:24:39+09:00</et>
            <sr>/starterkittest/v1_0/remoteCSE-nodeid_bang1/container-LoRa/subscription-gateway</sr>
            <st>14</st>
            <cr>RC00000000000000383527</cr>
            <cnf>text</cnf>
            <cs>8</cs>
            <con>4130363030303536373032303034313034303739323338313030303030303030</con>
        </m2m:cin>
    </pc>
</m2m:req>
 => ("nodeid_bang1", "4130363030303536373032303034313034303739323338313030303030303030")
 */
object DeviceDataExtractorFromOneM2MNotification extends StrictLogging {
  def extract(mqttMessage : MqttMessage) = {

    val oneM2MResource = ResourceFactoryLora.getDefault().parseRequestPrimitiveXml(mqttMessage.payload.utf8String).getContent
    oneM2MResource match {
        case cin: ContentInstanceLora =>
          val source  = cin.getSource.split("/")
          val serviceId = source(1)
          val deviceName = source(3).substring(10)
          val result = (serviceId, deviceName, cin.getContent)
          logger.info("Extracting completed : " + result)
          logger.info(s"contentInstance resourceName : ${cin.getResourceName}" )
          result
        case _ =>  logger.info("Unknown Lora Resource!!!")
      }
  }
}
