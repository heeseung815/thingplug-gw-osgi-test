package com.sk.thingplug.gw.stages.flow

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.json.XML

import scala.xml.XML._

/**
  * Created by Cho on 2017-06-19.
  */
class XmlConverter[T] extends GraphStage[FlowShape[T, String]] with StrictLogging {
  val in = Inlet[T]("XmlConverter.in")
  val out = Outlet[String]("XmlConverter.out")
  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    setHandler(in, new InHandler {
      override def onPush() = grab(in) match {
        case record: ConsumerRecord[Array[Byte], String] => push(out, XmlConverter.xmlToJson(record.value))
        case _ => logger.error("Not supported type")
      }
    })
    setHandler(out, new OutHandler {
      override def onPull = pull(in)
    })
  }
}

/*
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
            <con>31,60,80</con>
        </m2m:cin>
    </pc>
</m2m:req>
=>
{
  "m2m:req":{
    "xmlns:m2m":"http://www.onem2m.org/xml/protocols",
    "op":1,
    "pc":{
      "m2m:cin":{
        "st":14,
        "con":"31,60,80",   //TTV, CSV, JSON
        "ty":4,
        "lt":"2017-08-02T18:24:39+09:00",
        "et":"2017-09-01T18:24:39+09:00",
        "cr":"RC00000000000000383527",
        "cs":8,
        "ct":"2017-08-02T18:24:39+09:00",
        "ri":"CI00000000000084949738",
        "pi":"CT00000000000000006475",
        "cnf":"text",
        "rn":"CI00000000000084949738",
        "sr":"/starterkittest/v1_0/remoteCSE-nodeid_bang1/container-LoRa/subscription-gateway"
      }
    },
    "xmlns:xsi":"http://www.w3.org/2001/XMLSchema-instance",
    "to":"bang9211_gateway_subscriber",
    "rqi":"dbdd7c24-7494-439d-9211-e32485e9665b",
    "fr":"nodeid_bang1"
  }
}
*/
object XmlConverter extends StrictLogging {
  private def xmlToJson(xmlString: String) = {
    val result = XML.toJSONObject(loadString(xmlString).child.mkString).toString
    logger.info("Converting completed : " + result)
    result
  }
}