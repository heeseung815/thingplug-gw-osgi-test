package com.sk.thingplug.gw.export.datalake.hdfs

import java.io.PrintWriter
import java.sql.Timestamp

import akka.actor.{ActorRef, Cancellable, Scheduler}
import com.sk.thingplug.gw.GatewayConfig
import com.sk.thingplug.gw.export.datalake.DataLakeActor.Timeout
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FSDataOutputStream, FileSystem, Path}
import spray.json._

object MapJsonProtocol extends DefaultJsonProtocol {
  implicit object MapJsonFormat extends  JsonFormat[Map[String, Any]] {
    def write(m: Map[String, Any]) = {
      JsObject(m.mapValues {
        case v: String => JsString(v)
        case v: Int => JsNumber(v)
        case v: Long => JsNumber(v)
        case v: BigInt => JsNumber(v.longValue())
        case v: Double => JsNumber(v)
        case v: Float => JsNumber(v.toDouble)
        case v: Boolean => JsBoolean(v)
        case v: Timestamp => JsString(v.toString)
        case v: Seq[Int] => v.toJson
        case v: Array[Int] => v.toJson
        case v: Map[String, Any] => write(v)
        case _ => JsNull
      })
    }

    def read(value: JsValue) = ???
  }
}

object JsonFileWriter extends StrictLogging {
  def apply(nodeId: String, hdfsConfig: Configuration, scheduler: Scheduler, dataLakeActor: ActorRef) = new JsonFileWriter(nodeId, hdfsConfig, scheduler, dataLakeActor)

  val config: Config = GatewayConfig.config
  val fileSizeLimit: Int = if (config.hasPath("thingplug.export.datalake.hdfs.file-size-limit")) config.getInt("thingplug.export.datalake.hdfs.file-size-limit") * 1024 else 200 // b
  val fileTimeLimit: Int = if (config.hasPath("thingplug.export.datalake.hdfs.file-time-limit")) config.getInt("thingplug.export.datalake.hdfs.file-time-limit") else 5 // sec
  val basePath: String = if (config.hasPath("thingplug.export.datalake.hdfs.file-base-path")) config.getString("thingplug.export.datalake.hdfs.file-base-path") else "/data"
  val schemaString: String = if (config.hasPath("thingplug.export.datalake.hdfs.file-json-schema")) config.getString("thingplug.export.datalake.hdfs.file-json-schema") else "msgType,nodeType,nodeId,frequency,shaft,byteUnit,maxIndex,z,batteryVoltage,temperature,timestamp"
  val fileExtension: String = ".json"
}

class JsonFileWriter(val nodeId: String, val hdfsConfig: Configuration, val scheduler: Scheduler, val dataLakeActor: ActorRef) extends StrictLogging {
  import JsonFileWriter._

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._
  import scala.language.postfixOps

  import MapJsonProtocol._

  var path: Path = _
  private val fs: FileSystem = FileSystem.get(hdfsConfig)

  private var timerCancelHandle: Cancellable = _

  def addData(data: Map[String, Any]): Unit = {
    if (path == null) setPath

    if (!fs.exists(path)) {
      logger.info("===> File not exist")
      createFile
    } else {
      logger.info("===> File exist")
      checkFileSize
    }

    val convertedData: Map[String, Any] = data.get("timestamp") match { case Some(v: Long) => data + ("timestamp" -> new Timestamp(v.asInstanceOf[Long])) }
    val keys: Array[String] = schemaString.replaceAll(" ", "").split(",")
    val parsedData: JsValue = convertedData.filterKeys(keys.contains(_)).toJson

    // TODO: 제거 예정
//    logger.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
//    logger.info(s"ParsedData: $parsedData")
//    logger.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")

    val isAppendable = fs.getConf.get("dfs.support.append").toBoolean
    logger.info(s"isAppendable: $isAppendable")
    if (isAppendable) {
      val os: FSDataOutputStream = fs.append(path)
      val writer = new PrintWriter(os)
      writer.append(parsedData + "\n")
      writer.flush()
      os.hflush()
      writer.close()
      os.close()
    }
  }

  private def checkFileSize: Unit = {
    val fileStatus = fs.getFileStatus(path)
    logger.info(s"Current size: ${fileStatus.getLen}")
    if (fileStatus.getLen >= fileSizeLimit) {
      logger.info("Exceed file size limit")
      if (!timerCancelHandle.isCancelled) {
        logger.info("===> Timer cancel")
        timerCancelHandle.cancel()
      }
      setPath
      createFile
    }
  }

  def setPath: Unit = {
    TimeUtil.resetDate
    val filename: String = nodeId + "_" + TimeUtil.getCurrentTime + fileExtension
    path = new Path(basePath + "/" + TimeUtil.getCurrentYear + "/" + TimeUtil.getCurrentMonth + "/" + TimeUtil.getCurrentDay + "/" + filename)
  }

  def createFile: Unit = {
    fs.create(path).close()
    timerCancelHandle = scheduler.scheduleOnce(fileTimeLimit seconds, dataLakeActor, Timeout(nodeId))
    logger.info(s"File[${path.getName} is created for $nodeId")
  }
}