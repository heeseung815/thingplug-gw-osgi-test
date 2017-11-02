package com.sk.thingplug.gw.stages.flow

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.typesafe.scalalogging.StrictLogging

class TtvToThingplugDataConverter[T] extends GraphStage[FlowShape[Array[Map[String, Any]], String]] with StrictLogging {
  val in = Inlet[Array[Map[String, Any]]]("TtvToThingplugDataConverter.in")
  val out = Outlet[String]("TtvToThingplugDataConverter.out")
  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    setHandler(in, new InHandler {
      override def onPush() = grab(in) match {
        case ttvData: Array[Map[String, Any]] =>
          if(true) push(out, TtvToThingplugDataConverter.convertThingplugData(ttvData))
          else pull(in)
        case _ => logger.error("Not supported type")
      }
    })
    setHandler(out, new OutHandler {
      override def onPull = pull(in)
    })
  }
}

/** ttv data to thingplug ata example **/
// Array(
//  Map("type" -> "publicaccess","dataType" -> "boolean","value" -> false)
//  Map("type" -> "temperature","dataType" -> "float", "value" -> 35.6)
// )
// => {"publicaccess" : false} {"temperature" : 35.6}
object TtvToThingplugDataConverter extends StrictLogging {
  def convertThingplugData(ttvData : Array[Map[String, Any]]) = {
    //ttv를 thingplug 데이터 형식으로 변환  (EX)"Temperature" : 35.6
    var thingplugData = ""
    ttvData.foreach(
      ttv => { //Map("type" -> "publicaccess","dataType" -> "boolean","value" -> false)
        thingplugData += "{\"" + ttv.getOrElse("type", "not supported type") + "\" : " + ttv.getOrElse("value", None) + "}"
        if (ttv ne ttvData.last) thingplugData += " "
      }
    )
    thingplugData
  }
}
