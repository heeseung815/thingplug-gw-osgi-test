package com.sk.thingplug.gw.flow.preprocessing_vib.activation

import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger
import akka.stream._
import akka.stream.stage._
import com.sk.thingplug.api.GraphData
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._

/**
  * Created by kylee on 2017. 9. 8..
  */
class PreProcessingVibFlow(startTime: Long) extends GraphStage[FlowShape[GraphData, GraphData]] with StrictLogging {
  val in = Inlet[GraphData]("testFlow.in")
  val out = Outlet[GraphData]("testFlow.out")

  override val shape = FlowShape.of(in, out)

  val sdf = new SimpleDateFormat("YYYY/MM/dd/hh:mm:ss.SSS")
  var count = new AtomicInteger()
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
  //new GraphStageLogic(shape) {
    new TimerGraphStageLogic(shape) {

      setHandler(in, new InHandler {
        override def onPush() = grab(in) match {
          case data: GraphData =>{
            if (true) {
              scheduleOnce((data, System.currentTimeMillis()), startTime.milliseconds)
            }
            else pull(in)
          }
          case _ => println("Not supported type")
        }
      })

      setHandler(out, new OutHandler {
        override def onPull = pull(in)
      })

      override protected def onTimer(timerKey: Any): Unit = {

        val key = timerKey.asInstanceOf[Tuple2[GraphData, Long]]
        logger.info(s"pushed in PreProcessingVibFlow($startTime ms)[${count.incrementAndGet()}] : ${key._2} ms, ${sdf.format(new Date(key._2))}")
        push(out, key._1)
      }
    }
}


