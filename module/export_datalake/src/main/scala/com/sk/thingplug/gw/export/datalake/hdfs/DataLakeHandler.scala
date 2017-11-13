package com.sk.thingplug.gw.export.datalake.hdfs

import akka.actor.{ActorRef, Scheduler}
import com.sk.thingplug.gw.GatewayConfig
import com.sk.thingplug.gw.export.datalake.DataLakeConnection
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, LocalFileSystem}
import org.apache.hadoop.hdfs.DistributedFileSystem

import scala.language.postfixOps

object DataLakeHdfsHandler {
  def apply(scheduler: Scheduler, dataLakeActor: ActorRef): DataLakeHdfsHandler = new DataLakeHdfsHandler(scheduler, dataLakeActor)

  val config: Config = GatewayConfig.config
  val host: String = if (config.hasPath("thingplug.export.datalake.hdfs.hostname")) config.getString("thingplug.export.datalake.hdfs.hostname") else "localhost"
  val port: String = if (config.hasPath("thingplug.export.datalake.hdfs.port")) config.getString("thingplug.export.datalake.hdfs.port") else "9000"
  val user: String = if (config.hasPath("thingplug.export.datalake.hdfs.username")) config.getString("thingplug.export.datalake.hdfs.username") else "hscho"
  val defaultPathPrefix: String = if (config.hasPath("thingplug.export.datalake.hdfs.default-path-prefix")) config.getString("thingplug.export.datalake.hdfs.default-path-prefix") else "fs.defaultFS"
  val hdfsUri: String = "hdfs://" + host + ":" + port
  val hdfsConfig: Configuration = new Configuration()
  hdfsConfig.set(defaultPathPrefix, hdfsUri)
  hdfsConfig.setBoolean("fs.hdfs.impl.disable.cache", true)
  hdfsConfig.setInt("dfs.replication", 1)
  hdfsConfig.setBoolean("dfs.support.append", true)
  hdfsConfig.set("dfs.client.block.write.replace-datanode-on-failure.policy", "ALWAYS")
  hdfsConfig.setBoolean("dfs.client.block.write.replace-datanode-on-failure.enable", true)
  hdfsConfig.setBoolean("dfs.client.block.write.replace-datanode-on-failure.best-effort", true)
//  hdfsConfig.setClass("fs.file.impl", classOf[LocalFileSystem], classOf[FileSystem])
  hdfsConfig.setClass("fs.hdfs.impl", classOf[DistributedFileSystem], classOf[FileSystem])
  System.setProperty("HADOOP_USER_NAME", user)
}

class DataLakeHdfsHandler(scheduler: Scheduler, dataLakeActor: ActorRef) extends DataLakeConnection with StrictLogging {
  import DataLakeHdfsHandler._

  private var jsonFileWriterTable = Map.empty[String, JsonFileWriter]

  //  override def init(): Future[Any] = {
  //    logger.info(s"Hynix DataLake HDFS Connect options. defaultPathPrefix=$defaultPathPrefix, hdfsUri=$hdfsUri")
  //    var s: Socket = null
  //    Future {
  //      try {
  //        s = new Socket(host, port.toInt)
  //      } catch {
  //        case e: Exception => e
  //      } finally {
  //        if (s != null)
  //          s.close()
  //      }
  //    }
  //  }

  override def init(): Unit = {
    logger.info(s"Hynix DataLake HDFS Connect options. defaultPathPrefix=$defaultPathPrefix, hdfsUri=$hdfsUri")
  }

  override def onSensorTelemetryForJson(nodeId: String, data: Map[String, Any]): Unit = {
    logger.debug("===> onSensorTelemetryJson called.")

    jsonFileWriterTable.get(nodeId) match {
      case Some(jsonFileWriter) =>
        logger.debug("===> Use existing a JsonFileWriter.")
        jsonFileWriter.addData(data)
      case None =>
        logger.debug("===> Create a JsonFileWriter.")
        onSensorConnectForJson(nodeId) match {
          case Some(jsonFileWriter) => jsonFileWriter.addData(data)
          case None => logger.error(s"[$nodeId] Failed to create a json file.")
        }
    }
  }

  override def onSensorConnectForJson(nodeId: String): Option[JsonFileWriter] = {
    jsonFileWriterTable += (nodeId -> JsonFileWriter(nodeId, hdfsConfig, scheduler, dataLakeActor))
    jsonFileWriterTable.get(nodeId)
  }

  override def flushForJson(nodeId: String): Unit = {
    if (nodeId != null) {
      jsonFileWriterTable.get(nodeId) match {
        case Some(jsonFileWriter) =>
          logger.info(s"===> Node[$nodeId] timeout...")
          jsonFileWriter.path = null
        case None => logger.debug(s"[$nodeId] JsonFileWriter not exist.")
      }
    }
  }
}
