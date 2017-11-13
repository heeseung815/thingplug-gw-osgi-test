package hscho.test.service

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.alpakka.mqtt.{MqttConnectionSettings, MqttQoS, MqttSourceSettings}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, KillSwitches, UniqueKillSwitch}
import com.sk.thingplug.api._
import com.typesafe.scalalogging.StrictLogging
import hscho.test.GatewayConfig
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.osgi.framework.BundleContext


/**
  * Created by kylee on 2017. 9. 8..
  */

case class GraphSourceContext(bundleName:String, className:String, setting:Any, source: Source[Any, Any])
case class GraphFlowContext(bundleName:String, className:String, flow: Flow[Any, Any, Any])
case class GraphSinkContext(bundleName:String, className:String, setting:Any, sink: Sink[Any, Any])

class RunnableGraphBuilder(context:BundleContext, graphConfig:GraphConfig)(implicit system:ActorSystem, materializer:ActorMaterializer) extends StrictLogging{
  private var sourceMap: Map[String, Seq[GraphSourceContext]] = Map.empty
  private var flowMap: Map[String, GraphFlowContext] = Map.empty
  private var sinkMap: Map[String, Seq[GraphSinkContext]] = Map.empty

  sinkMap += ("ignore" -> Seq(GraphSinkContext("ignore", "ignore", None, Sink.ignore)))

  val config          = GatewayConfig.config

/*
  def reloadImportService(service: ImportService[Any, Any, Any, Any]): Unit = {
    logger.info(s"reloadImreloadImportServiceportService : ${service.toString}")
    service.bundleName match {
      case "skt.thingplug.import_lora" => {
        //logger.info("reloadImportService 1")
        /*
        val hostname: String = config.getString("thingplug.import.lora.mqtt.hostname")
        logger.info("reloadImportService 1-0")
        val port: Int = config.getInt("thingplug.import.lora.mqtt.port")
        logger.info("reloadImportService 1-0")
        val topic           = if (config.hasPath("thingplug.import.lora.mqtt.topic")) config.getString("thingplug.import.lora.mqtt.topic") else "test"
        logger.info("reloadImportService 1-0")
        val clientId        = if (config.hasPath("thingplug.import.lora.mqtt.clientid")) config.getString("thingplug.import.lora.mqtt.clientid") else "test"
        logger.info("reloadImportService 1-0")
        val userId          = if (config.hasPath("thingplug.import.lora.mqtt.userid")) config.getString("thingplug.import.lora.mqtt.userid") else "test"
        logger.info("reloadImportService 1-0")
        val password        = if (config.hasPath("thingplug.import.lora.mqtt.password")) config.getString("thingplug.import.lora.mqtt.password") else "test"
        */

        val hostname = "thingplugtest.sktiot.com"
        val port = 8883
        val topic = "/oneM2M/req_msg/+/bang9211_edge_subscriber"
        val clientId = "bang9211_edge_subscriber"
        val userId = "bang9211"
        val password = "K2thYmFLdnFOOE1uWlpKVHVsMlBudDFjQXZYbWR5V3p3dHcwRDl5ZEdxRU45ajMrQUt1Rm80Y1ZubktEcEswUw"

        //logger.info(s"reloadImportService 1-1. $hostname, $port, $topic, $clientId, $userId, $password")
        val connectionSettings = MqttConnectionSettings(
          "ssl://" + hostname + ":" + port,
          clientId,
          new MemoryPersistence
        ).withAuth(userId, password)

        //logger.info("reloadImportService 2")
        val settings = MqttSourceSettings(
          connectionSettings.withClientId(clientId),
          Map(topic -> MqttQoS.AtLeastOnce)
        )

        sourceMap += (service.className -> Seq(GraphSourceContext(service.bundleName, service.className, settings, service.broadcastHubSource(settings))))
        //logger.info(s"reloadImportService 3. sourceMap=${sourceMap.toString()}, className=${service.className}")

      }
      case u:Any => {
        logger.info(s"unknown bundle name. ${u.toString}")
        return
      }
    }


    //logger.info("reloadImportService 4")
    val runnableGraphConfigs = graphConfig.graph.filter(
      (runnableGraphConfig: RunnableGraphConfig) => {
        if (runnableGraphConfig.source == service.className) {
          logger.info(s"runnableGraphConfig.source=${runnableGraphConfig.source}, service.className=${service.className}")
          true
        } else
          false
      }
    )

    runnableGraphConfigs.foreach(
      (runnableGraphConfig: RunnableGraphConfig) => reload(runnableGraphConfig)
    )
  }

  def removeImportService(service: ImportService[Any, Any, Any, Any]): Unit = {
    logger.info(s"removeImportService : ${service.toString}")
    sourceMap -= service.className
    val runnableGraphConfigs = graphConfig.graph.filter(
      (runnableGraphConfig: RunnableGraphConfig) => {
        if (runnableGraphConfig.source == service.className) {
          logger.info(s"runnableGraphConfig.source=${runnableGraphConfig.source}, service.className=${service.className}")
          true
        } else
          false
      }
    ).foreach( (runnableGraphConfig: RunnableGraphConfig) => {
      runnableGraphConfig.killSwitch.foreach( (killSwitch:UniqueKillSwitch) => {
        killSwitch.shutdown()
      })
      runnableGraphConfig.killSwitch = Seq.empty

      logger.info(s"Succeed to remove ImportService : config=${runnableGraphConfig.toString}, sourceMap=${sourceMap.toString}")
    })
  }
*/
  def reloadExportService(service: ExportService[Any, Any, Any, Any]) : Unit = {
    logger.info(s"reloadExportService : ${service.toString}")
    service.bundleName match {
      case "skt.thingplug.export_thingplug" => {
        //logger.info("reloadBidExportService 1")
        sinkMap += (service.className -> Seq(GraphSinkContext(service.bundleName, service.className, None, service.mergeHubSink(config))))
      }
      case "skt.thingplug.export_datalake" => {
        println("DataLake service is reloaded..!")
        println("+++++++++++++++++++++++++++++++")
        println(s"service.className: ${service.className}")
        println(s"service.className: ${service.bundleName}")
        println("+++++++++++++++++++++++++++++++")
        sinkMap += (service.className -> Seq(GraphSinkContext(service.bundleName, service.className, None, service.mergeHubSink(config))))
      }
      case u:Any => {
        logger.info(s"unknown bundle name. ${u.toString}")
        return
      }
    }

    println(s"#1: ${graphConfig.graph.size}")
    //logger.info("reloadBidExportService 2")
    val runnableGraphConfigs: Seq[RunnableGraphConfig] = graphConfig.graph.filter(
      (runnableGraphConfig: RunnableGraphConfig) => {
        println("=============")
        println(s"sink: ${runnableGraphConfig.sink}")
        println(s"className: ${service.className}")
        println("=============")
        if (runnableGraphConfig.sink == service.className) {
          logger.info(s"runnableGraphConfig.sink=${runnableGraphConfig.sink}, service.className=${service.className}")
          true
        } else
          false
      }
    )

    println(s"#2: ${runnableGraphConfigs.size}")
    //logger.info(s"reloadBidExportService 3. runnableGraphConfigs=${runnableGraphConfigs.toString()}")
    runnableGraphConfigs.foreach(
      (runnableGraphConfig: RunnableGraphConfig) => reload(runnableGraphConfig)
    )
  }

  def removeExportService(service: ExportService[Any, Any, Any, Any]) : Unit = {
    logger.info(s"removeBidExportService : ${service.toString}")
    sinkMap -= service.className

    val runnableGraphConfigs = graphConfig.graph.filter(
      (runnableGraphConfig: RunnableGraphConfig) => {
        if (runnableGraphConfig.sink == service.className) {
          logger.info(s"runnableGraphConfig.sink=${runnableGraphConfig.sink}, service.className=${service.className}")
          true
        } else
          false
      }
    ).foreach( (runnableGraphConfig: RunnableGraphConfig) => {
      runnableGraphConfig.killSwitch.foreach( (killSwitch:UniqueKillSwitch) => {
        killSwitch.shutdown()
      })
      runnableGraphConfig.killSwitch = Seq.empty

      logger.info(s"Succeed to remove BidExportService : config=${runnableGraphConfig.toString}, sourceMap=${sourceMap.toString}, sinkMap=${sinkMap.toString()}")
    })
  }
/*
  def reloadBidExportService(service: BidExportService[Any, Any, Any, Any, Any, Any]) : Unit = {
    logger.info(s"reloadBidExportService : ${service.toString}")
    service.bundleName match {
      case "skt.thingplug.export_thingplug" => {
        //logger.info("reloadBidExportService 1")
        sinkMap += (service.className -> Seq(GraphSinkContext(service.bundleName, service.className, None, service.mergeHubSink(config))))
        sourceMap += (service.className -> Seq(GraphSourceContext(service.bundleName, service.className, None, service.source(config))))
      }
      case u:Any => {
        logger.info(s"unknown bundle name. ${u.toString}")
        return
      }
    }


    //logger.info("reloadBidExportService 2")
    val runnableGraphConfigs = graphConfig.graph.filter(
      (runnableGraphConfig: RunnableGraphConfig) => {
        if (runnableGraphConfig.sink == service.className) {
          logger.info(s"runnableGraphConfig.sink=${runnableGraphConfig.sink}, service.className=${service.className}")
          true
        } else if (runnableGraphConfig.source == service.className){
          logger.info(s"runnableGraphConfig.source=${runnableGraphConfig.source}, service.className=${service.className}")
          true
        } else
          false
      }
    )

    //logger.info(s"reloadBidExportService 3. runnableGraphConfigs=${runnableGraphConfigs.toString()}")
    runnableGraphConfigs.foreach(
      (runnableGraphConfig: RunnableGraphConfig) => reload(runnableGraphConfig)
    )
  }

  def removeBidExportService(service: BidExportService[Any, Any, Any, Any, Any, Any]) : Unit = {
    logger.info(s"removeBidExportService : ${service.toString}")
    sinkMap -= service.className
    sourceMap -= service.className

    val runnableGraphConfigs = graphConfig.graph.filter(
      (runnableGraphConfig: RunnableGraphConfig) => {
        if (runnableGraphConfig.sink == service.className) {
          logger.info(s"runnableGraphConfig.sink=${runnableGraphConfig.sink}, service.className=${service.className}")
          true
        } else if (runnableGraphConfig.source == service.className){
          logger.info(s"runnableGraphConfig.source=${runnableGraphConfig.source}, service.className=${service.className}")
          true
        } else
          false
      }
    ).foreach( (runnableGraphConfig: RunnableGraphConfig) => {
      runnableGraphConfig.killSwitch.foreach( (killSwitch:UniqueKillSwitch) => {
        killSwitch.shutdown()
      })
      runnableGraphConfig.killSwitch = Seq.empty

      logger.info(s"Succeed to remove BidExportService : config=${runnableGraphConfig.toString}, sourceMap=${sourceMap.toString}, sinkMap=${sinkMap.toString()}")
    })
  }

  def reloadFlowService(service: FlowService[Any, Any, Any, Any]): Unit = {
    logger.info(s"reloadFlowService : ${service.toString}")

    flowMap += (service.className -> GraphFlowContext(service.bundleName, service.className, service.flow))

    val runnableGraphConfigs = graphConfig.graph.filter(
      (runnableGraphConfig: RunnableGraphConfig) => {
        if (runnableGraphConfig.flow.contains(service.className)) {
          logger.info(s"runnableGraphConfig.sink=${runnableGraphConfig.flow.toString}, service.className=${service.className}")
          true
        } else
          false
      }
    )

    runnableGraphConfigs.foreach(
      (runnableGraphConfig: RunnableGraphConfig) => reload(runnableGraphConfig)
    )
  }

  def removeFlowService(service: FlowService[Any, Any, Any, Any]): Unit = {
    logger.info(s"reloadFlowService : ${service.toString}")
    flowMap -= service.className
    val runnableGraphConfigs = graphConfig.graph.filter(
      (runnableGraphConfig: RunnableGraphConfig) => {
        if (runnableGraphConfig.flow.contains(service.className)) {
          logger.info(s"runnableGraphConfig.flow=${runnableGraphConfig.flow.toString}, service.className=${service.className}")
          true
       } else
          false
      }
    ).foreach( (runnableGraphConfig: RunnableGraphConfig) => {
      runnableGraphConfig.killSwitch.foreach( (killSwitch:UniqueKillSwitch) => {
        killSwitch.shutdown()
      })
      runnableGraphConfig.killSwitch = Seq.empty
      logger.info(s"Succeed to remove removeFlowService : config=${runnableGraphConfig.toString}, flowMap=${flowMap.toString}")
    })
  }
*/
  def reload(runnableGraphConfig: RunnableGraphConfig) :Unit = {
    println(s"#3: ${sourceMap.size}")
/*
    val sourceContextSeq: Option[Seq[GraphSourceContext]] = sourceMap.get(runnableGraphConfig.source)
    logger.info(s"build: config=${runnableGraphConfig}, context=${sourceContextSeq.toString}")
    sourceContextSeq match {
      case Some(s: Seq[GraphSourceContext]) => logger.info(s"Succeed to find source graph. name=${runnableGraphConfig.source}, service=${s.toString}")
      case None => {
        logger.info(s"Fail to find source graph. name=${runnableGraphConfig.source}")
        return
      }
    }
    var sourceSeq: Seq[Source[Any, Any]] = Seq.empty
    sourceContextSeq.get.foreach( (sourceContext:GraphSourceContext) => sourceSeq ++= Seq(sourceContext.source))

    var flowSeq: Seq[Flow[Any, Any, Any]] = Seq.empty
    var flowContextSeq: Seq[Option[GraphFlowContext]] = Seq.empty
    if (runnableGraphConfig.flow != null) {
      runnableGraphConfig.flow.foreach( (flowName:String) => {
        val flowContext = flowMap.get (flowName)
        flowContextSeq ++=  Seq(flowContext)
        logger.info (s"build: config=${runnableGraphConfig}, context=${flowContextSeq.toString}")
        flowContext match {
          case Some (s: GraphFlowContext) => logger.info (s"Succeed to find flow graph. name=${runnableGraphConfig.flow.toString}, service=${s.toString}")
          case None => {
            logger.info (s"Fail to find flow graph. name=${runnableGraphConfig.flow.toString}")
            return
          }
        }
        flowSeq ++= Seq(flowContext.get.flow)
      })
    }
*/

    val sinkContextSeq: Option[Seq[GraphSinkContext]] = sinkMap.get(runnableGraphConfig.sink)
    logger.info(s"build: config=${runnableGraphConfig}, context=${sinkContextSeq.toString}")
    sinkContextSeq match {
      case Some(s: Seq[GraphSinkContext]) => logger.info(s"Succeed to find sink graph. name=${runnableGraphConfig.sink}, service=${s.toString}")
      case None => {
        logger.info(s"Fail to find sink graph. name=${runnableGraphConfig.sink}")
        return
      }
    }
    var sinkSeq: Seq[Sink[Any, Any]] = Seq.empty
    sinkContextSeq.get.foreach( (sinkContext:GraphSinkContext) => sinkSeq ++= Seq(sinkContext.sink))

    runnableGraphConfig.killSwitch.foreach( (killSwitch:UniqueKillSwitch) => {
      logger.warn(s"Already exist runnable graph. shutdown the graph(${runnableGraphConfig.toString}")
      killSwitch.shutdown()
    })
    runnableGraphConfig.killSwitch = Seq.empty

    val snr001_data01: Map[String, Any] = Map[String, Any](
      "nodeId" -> "sensor001",
      "frequency" -> 111,
      "x" -> Array(1, 1, 1, 1, 1),
      "y" -> Array(2, 2, 2, 2, 2),
      "z" -> Array(3, 3, 3, 3, 3),
      "timestamp" -> System.currentTimeMillis())
    val snr001_data02: Map[String, Any] = Map[String, Any](
      "nodeId" -> "sensor001",
      "frequency" -> 111111,
      "x" -> Array(10, 10, 10, 10, 10),
      "y" -> Array(20, 20, 20, 20, 20),
      "z" -> Array(30, 30, 30, 30, 30),
      "timestamp" -> System.currentTimeMillis())
    val snr002_data01: Map[String, Any] = Map[String, Any](
      "nodeId" -> "sensor002",
      "frequency" -> 222,
      "x" -> Array(10000, 10000, 10000, 10000, 10000),
      "y" -> Array(20000, 20000, 20000, 20000, 20000),
      "z" -> Array(30000, 30000, 30000, 30000, 30000),
      "timestamp" -> System.currentTimeMillis())


    val graphDataList = List(new GraphData("sensor001", null, null, Seq(snr001_data01)), new GraphData("sensor002", null, null, Seq(snr002_data01)), new GraphData("sensor001", null, null, Seq(snr001_data02)))
//    val temp = Source(1 to 10).viaMat(KillSwitches.single)(Keep.right).async
    val testSource: Source[GraphData, UniqueKillSwitch] = Source(graphDataList).viaMat(KillSwitches.single)(Keep.right).async

    println("# TRY!!")
    sinkSeq.foreach( (sink:Sink[Any, Any]) =>  {
      runnableGraphConfig.killSwitch ++= Seq(testSource.to(sink).run()(materializer))
      logger.info(s"Succeed to run graph(${runnableGraphConfig.toString}).\n sink=$sinkContextSeq, killSwitch=${runnableGraphConfig.killSwitch.toString()}")
    })
/*
    sourceSeq.foreach( (source: Source[Any, Any]) => {
      var graph = source.viaMat(KillSwitches.single)(Keep.right).async
      flowSeq.foreach( (flow:Flow[Any, Any, Any]) => {
        graph = graph.via(flow)
      })
      sinkSeq.foreach( (sink:Sink[Any, Any]) =>  {
        runnableGraphConfig.killSwitch ++= Seq(graph.to(sink).run()(materializer))
        logger.info(s"Succeed to run graph(${runnableGraphConfig.toString}).\n source=$sourceContextSeq, flowSeq=$flowContextSeq, sink=$sinkContextSeq, killSwitch=${runnableGraphConfig.killSwitch.toString()}")
      })
    })
*/
  }

  def reload() : Unit = {
    graphConfig.graph.foreach(
      (runnableGraphConfig: RunnableGraphConfig) => reload(runnableGraphConfig)
    )
  }
}
