package com.sk.thingplug.gw.stages.flow

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.typesafe.scalalogging.StrictLogging

class GasAMIDataConverter  extends GraphStage[FlowShape[(String, String, String), (String, String, Map[String, Any])]] with StrictLogging {
  val in = Inlet[(String, String, String)]("GasAMIDataConverter.in")
  val out = Outlet[(String, String, Map[String, Any])]("GasAMIDataConverter.out")
  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    setHandler(in, new InHandler {
      override def onPush() = grab(in) match {
        case gasAMIData: (String, String, String) =>
          if(true) push(out, GasAMIDataConverter.convert(gasAMIData))
          else pull(in)
        case _ => logger.error("Not supported type")
      }
    })
    setHandler(out, new OutHandler {
      override def onPull = pull(in)
    })
  }
}

/*
("nodeid_bang1", "4130363030303536373032303034313034303739323338313030303030303030")  //(ASCII Hex)
= A0     6000267   020   0410    4079   2381    00000000
 정보유형    ID      길이    잔량1    잔량2  배터리잔량   미터기값
=> Map(
    "type" -> A0,
    "id" -> 6000267,
    "length" -> 020,
    "remains1" -> 0410,
    "remains2" -> 4079,
    "battery_remains" -> 2381
    "meterValue" -> 00000000
   )
*/
object GasAMIDataConverter extends StrictLogging {
  def validation(gasAMIData: (String, String, String)) = {
    if(gasAMIData._3.length != 64) {
      logger.error("Invalid GasAMI data");
      false
    }
      /*
    else if(!deviceList.contains(gasAMIData._1)) {
      logger.error("Invalid Device ID")
      false
    }
    */
    else
      true
  }

  def convert(gasAMIData: (String, String, String)) = {
    val dataString = gasAMIData._3.split("(?<=\\G.{2})").map(s => Integer.parseInt(s, 16).toChar).mkString
    //4130363030303536... => "A0600026..."
    var gasAMIDataMap: Map[String, Any] = Map.empty
    //gasAMIDataMap += ("type" -> dataString.substring(0, 2))
    gasAMIDataMap += ("sensornodeid" -> dataString.substring(2, 9))
    //gasAMIDataMap += ("length" -> dataString.substring(9, 12))
    gasAMIDataMap += ("remains1" -> dataString.substring(12, 16).toInt)
    gasAMIDataMap += ("remains2" -> dataString.substring(16, 20).toInt)
    gasAMIDataMap += ("battery_remains" -> dataString.substring(20, 24).toInt)
    gasAMIDataMap += ("meterValue" -> dataString.substring(24, 32).toInt)
    gasAMIDataMap += ("timestamp" -> System.currentTimeMillis())
    /*
    gasAMIDataMap += ("test_boolean" -> true)
    gasAMIDataMap += ("test_double" -> 1.345656899)
    gasAMIDataMap += ("test_map" -> Map("test_string" -> "hello", "test_int" -> 123))
    gasAMIDataMap += ("test_array" -> Seq("123", "456", "789", 1234566))
    gasAMIDataMap += ("test_array2" -> Seq(Map("test_string" -> "hello", "test_int" -> 123), Map("test_string" -> "world", "test_int" -> 456)))
    */
    val result = gasAMIDataMap
    logger.info("Converting completed : " + result)
    (gasAMIData._1, gasAMIData._2, result)
  }
}