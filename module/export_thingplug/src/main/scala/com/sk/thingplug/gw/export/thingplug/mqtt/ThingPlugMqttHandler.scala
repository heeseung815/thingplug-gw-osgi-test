package com.sk.thingplug.gw.export.thingplug.mqtt

import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicInteger

import akka.stream.alpakka.mqtt.MqttConnectionSettings
import com.sk.thingplug.api.{GraphData, GraphMessageType}
import com.sk.thingplug.gw.GatewayConfig
import com.sk.thingplug.gw.export.thingplug.ThingPlugConnection
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.eclipse.paho.client.mqttv3.{IMqttActionListener, IMqttDeliveryToken, IMqttToken, MqttAsyncClient, MqttCallback, MqttConnectOptions, MqttMessage => PahoMqttMessage}

import scala.concurrent.{ExecutionContext, Future, Promise}
import collection.JavaConverters._
import scala.collection.concurrent.TrieMap
import scala.concurrent.duration._
import spray.json._


case class ServiceInfo (var id:String, var deviceDescriptor:String, var telemetryPeriod:Long)
case class DeviceInfo (var deviceName:String, var telemetryTimeStamp:Long, var telemetryPeriod:Long, var serviceId: String)
case class StatisticInfo (var devicesOnline:AtomicInteger, var attributesUploaded:AtomicInteger, var telemetryUploaded:AtomicInteger)


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
        case v: Map[String, Any] => write(v)
        //case v: Seq[Map[String, Any]] => JsArray(v.map( vv => write(vv)))
        case _ => JsNull
      })
    }

    def read(value: JsValue) = ???
  }
}

import MapJsonProtocol._

object ThingPlugMqttHandler {

  def apply(): ThingPlugMqttHandler = new ThingPlugMqttHandler()
}

/**
  * Created by mrhjkim on 2017. 7. 24..
  */
class ThingPlugMqttHandler() extends ThingPlugConnection with StrictLogging  {
  import scala.concurrent.ExecutionContext.Implicits.global

  var devices = TrieMap.empty[String, DeviceInfo]
  var services = Map.empty[String, ServiceInfo]
  var statistic = StatisticInfo(new AtomicInteger, new AtomicInteger, new AtomicInteger)

  val config: Config = GatewayConfig.config
  val gwName: String = if (config.hasPath("thingplug.export.thingplug.gwname")) config.getString("thingplug.export.thingplug.gwname") else "testbed1"
  val svcName: String = if (config.hasPath("thingplug.export.thingplug.svcname")) config.getString("thingplug.export.thingplug.svcname") else "serviceId123"
  val tp2host: String = if (config.hasPath("thingplug.export.thingplug.mqtt.hostname")) config.getString("thingplug.export.thingplug.mqtt.hostname") else "127.0.0.1"
  val tp2port: String = if (config.hasPath("thingplug.export.thingplug.mqtt.port")) config.getString("thingplug.export.thingplug.mqtt.port") else "1883"
  val accessToken: String = if (config.hasPath("thingplug.export.thingplug.mqtt.access_token")) config.getString("thingplug.export.thingplug.mqtt.access_token") else "testbed1"

  val servicesConfig = config.getObjectList("thingplug.export.thingplug.service").asScala
  //logger.info(s"servicesConfig list : ${servicesConfig.toString}")
  servicesConfig.foreach(config => {
    val idList = config.toConfig.getStringList("id").asScala
    val deviceDescriptor = config.toConfig.getString("deviceDescriptor")
    val telemetryPeriod = config.toConfig.getLong("telemetryPeriod")
    //logger.info(s"service id(${idList.toString()}), deviceDescripor($deviceDescriptor), telemetryPeriod($telemetryPeriod)")
    idList.foreach( id => services += (id -> ServiceInfo(id, deviceDescriptor, telemetryPeriod)) )
  } )
  logger.info(s"services : ${services.toString()}")

  val connectionSettings: MqttConnectionSettings = MqttConnectionSettings(
    s"tcp://$tp2host:$tp2port",
    gwName,
    new MemoryPersistence
  ).withAuth(accessToken, "")
    .withCleanSession(false)

  val GATEWAY_CONNECT_TOPIC = s"v1/gw/$svcName/$gwName/connect"
  val GATEWAY_DISCONNECT_TOPIC = s"v1/gw/$svcName/$gwName/disconnect"
  val GATEWAY_ATTRIBUTES_TOPIC = s"v1/gw/$svcName/$gwName/attributes"
  val GATEWAY_TELEMETRY_TOPIC = s"v1/gw/$svcName/$gwName/telemetry"
  val GATEWAY_STATISTICS_TOPIC = s"v1/dev/$svcName/$gwName/telemetry"
  val GATEWAY_DEVICE_CONTROL_UP_TOPIC = s"v1/gw/$svcName/$gwName/up"
  val GATEWAY_DEVICE_CONTROL_DOWN_TOPIC = s"v1/gw/$svcName/$gwName/down"

  val msgIdSeq = new AtomicInteger()

  var client:MqttAsyncClient = null

  override def init(connectionLostUserFunc:Throwable => Unit): Future[IMqttToken] = {
    client = new MqttAsyncClient(
      connectionSettings.broker,
      connectionSettings.clientId,
      connectionSettings.persistence
    )

    logger.info(s"ThingPlug MQTT Connect options. broker=${connectionSettings.broker}, clientId=${connectionSettings.clientId}")

    client.setCallback(new MqttCallback {
      def messageArrived(topic: String, message: PahoMqttMessage): Unit = {
        if (topic == GATEWAY_ATTRIBUTES_TOPIC) onAttribute(message)
        else if (topic == GATEWAY_DEVICE_CONTROL_DOWN_TOPIC) onRpc(message)
      }

      def deliveryComplete(token: IMqttDeliveryToken): Unit = {
        logger.info(s"Delivery complete. token=${token.toString}")
      }

      def connectionLost(cause: Throwable): Unit = {
        logger.error(s"ThingPlug MQTT Connection Lost. cause=${cause.getLocalizedMessage}")
        connectionLostUserFunc(cause)
      }
    })

    val connectOptions = new MqttConnectOptions
    connectionSettings.auth.foreach {
      case (user, password) =>
        connectOptions.setUserName(user)
        connectOptions.setPassword(password.toCharArray)
    }
    connectionSettings.socketFactory.foreach { socketFactory =>
      connectOptions.setSocketFactory(socketFactory)
    }
    connectionSettings.will.foreach { will =>
      connectOptions.setWill(will.message.topic, will.message.payload.toArray, will.qos.byteValue.toInt, will.retained)
    }
    connectOptions.setCleanSession(connectionSettings.cleanSession)
    connectOptions.setMaxInflight(10000)

    val p = Promise[IMqttToken]
    client.connect(connectOptions, (), new IMqttActionListener {
      def onSuccess(iMqttToken: IMqttToken): Unit = {
        logger.info("Success to connect ThingPlug")
        devices.map{ case (k, v) => onDeviceConnect(GraphData(k, v.serviceId, GraphMessageType.Connect, null))}
        p.success(iMqttToken)
      }

      def onFailure(iMqttToken: IMqttToken, e: Throwable): Unit = {
        logger.error(s"Fail to connect ThingPlug. e=${e.getLocalizedMessage}")
        p.failure(e)
      }
    })
    p.future
  }

/*
  def parseJsonValueAny(value:Any) : JsValue = {
    value match {
      case value: String => JsString(value)
      case value: Int => JsNumber(value)
      case value: Long => JsNumber(value)
      case value: BigInt => JsNumber(value.longValue())
      case value: Double => JsNumber(value)
      case value: Float => JsNumber(value.toDouble)
      case value: Boolean => JsBoolean(value)
      case value: Map[String, Any] => {
        var ret = Json.obj()
        value.foreach { case (k: String, v: Any) => ret = ret + (k, parseJsonValueAny(v)) }
        ret
      }
      case value: Seq[Any] => JsArray(value.map(v => parseJsonValueAny(v)))
      case _ => JsNull
    }
  }

  val loraJsonWriter = Writes[Map[String, Any]] ( o => {
    var ret = Json.obj()
    o.foreach{
      case (k:String, v:Any) => ret = ret + (k, parseJsonValueAny(v))
      case (k:String, null) => ret = ret + (k, parseJsonValueAny(null))
      case any =>  logger.error("Unexpected input. key=" + any)
    }
    ret
  })
*/


  def processTelemetry(data:GraphData, deviceInfo:DeviceInfo): Future[IMqttToken] = {
    val timestamp = System.currentTimeMillis()
    val indexedSeq = data.message.map( m => m.toJson).toVector
    //Json.toJson()
    val json: JsValue = JsObject(data.deviceName -> JsArray(indexedSeq))
    logger.info(s"Json: ${json.toString()}")

    val msgId = msgIdSeq.incrementAndGet()
    val pahoMsg = new PahoMqttMessage(json.toString().getBytes(StandardCharsets.UTF_8))
    pahoMsg.setId(msgId)
    pahoMsg.setQos(1)

    pubilsh(
      GATEWAY_TELEMETRY_TOPIC,
      pahoMsg,
      (token:IMqttToken) =>  {
        logger.info(s"Device(${data.deviceName}) telemetry event is reported to ThingPlug. telemetry=${json.toString()}")
        deviceInfo.telemetryTimeStamp = timestamp
        devices(data.deviceName) = deviceInfo
        statistic.telemetryUploaded.incrementAndGet()
      },
      (token:IMqttToken, e:Throwable) =>  logger.error(s"Failed to report device(${data.deviceName}) telemetry event.e=${e.getLocalizedMessage}")
    )
  }

  override def onDeviceTelemetry(data:GraphData): Future[IMqttToken] = {
    //logger.info(s"devices : ${devices.toString()}")
    devices.get(data.deviceName) match {
      case Some(deviceInfo) => {
        //logger.info(s"Device($deviceName) find")
        processTelemetry(data, deviceInfo)
      }
      case None => {
        DelayedFuture(100.milliseconds)(processTelemetry(data, devices.get(data.deviceName).get)).flatten
        onDeviceConnect(data)
      }
    }
  }

  override def onDeviceAttribute(data:GraphData): Future[Any] = ???

  override def onDeviceConnect(data:GraphData): Future[IMqttToken] = {
    val service = services.get(data.serviceId)
    service match {
      case Some(serviceInfo) => {
        val json: JsValue = JsObject("device" -> JsString(data.deviceName), "deviceDescriptor" -> JsString(serviceInfo.deviceDescriptor))
        val msgId = msgIdSeq.incrementAndGet()

        logger.info(s"Json: ${json.toString()}")
        val pahoMsg = new PahoMqttMessage(json.toString().getBytes(StandardCharsets.UTF_8))
        pahoMsg.setId(msgId)
        pahoMsg.setQos(1)

        pubilsh(
          GATEWAY_CONNECT_TOPIC,
          pahoMsg,
          (token:IMqttToken) =>  {
            logger.info(s"Device(${data.deviceName}) connect event is reported to ThingPlug")
            devices += (data.deviceName -> DeviceInfo(data.deviceName, 0, serviceInfo.telemetryPeriod, serviceInfo.id))
            statistic.devicesOnline.incrementAndGet()
          },
          (token:IMqttToken, e:Throwable) =>  logger.error(s"Failed to report device(${data.deviceName}) connect event.e=${e.getLocalizedMessage}")
        )
      }
      case None => {
        logger.error(s"Fail to find service. id=${data.serviceId}")
        Future.failed(new Throwable(s"Fail to find service. id=${data.serviceId}"))
      }
    }
  }

  override def onDeviceDisconnect(data:GraphData): Future[IMqttToken] = {
    val json: JsValue = JsObject("device" -> JsString(data.deviceName))
    val msgId = msgIdSeq.incrementAndGet()

    val pahoMsg = new PahoMqttMessage(json.toString().getBytes(StandardCharsets.UTF_8))
    pahoMsg.setId(msgId)
    pahoMsg.setQos(1)

    pubilsh(
      GATEWAY_DISCONNECT_TOPIC,
      pahoMsg,
      (token:IMqttToken) =>  {
        logger.info(s"Device(${data.deviceName}) disconnect event is reported to ThingPlug")
        devices -= data.deviceName
        statistic.devicesOnline.decrementAndGet()
      },
      (token:IMqttToken, e:Throwable) =>  logger.error(s"Failed to report device(${data.deviceName}) disconnect event.e=${e.getLocalizedMessage}")
    )
  }

  override def onAttribute(message:Any): Future[Any] = ???

  override def onRpc(message:Any): Future[Any] = ???

  override def onTelemetryTick(): Unit = {
    val timestamp = System.currentTimeMillis()
    devices.filter{ case (_, deviceInfo:DeviceInfo) =>  (timestamp - deviceInfo.telemetryTimeStamp) > deviceInfo.telemetryPeriod}
      .foreach{ case (deviceName:String, deviceInfo: DeviceInfo) => {
        logger.info(s"Device($deviceName) timeout. telemetryPeriod=${deviceInfo.telemetryPeriod}, lastTimestamp=${deviceInfo.telemetryTimeStamp}, currentTimestamp=$timestamp, diff=${timestamp - deviceInfo.telemetryTimeStamp}")
        onDeviceDisconnect(GraphData(deviceName, null, GraphMessageType.Disconnect, null))
      }
      }
  }

  override def onStatisticTick(): Unit = {
    val data: JsValue = JsObject(
      "timestamp" -> JsNumber(System.currentTimeMillis()),
      "devicesOnline" -> JsNumber(statistic.devicesOnline.get()),
      "attributesUploaded" -> JsNumber(statistic.attributesUploaded.get()),
      "telemetryUploaded" -> JsNumber(statistic.telemetryUploaded.get()))
    val msgId = msgIdSeq.incrementAndGet()

    val pahoMsg = new PahoMqttMessage(data.toString().getBytes(StandardCharsets.UTF_8))
    pahoMsg.setId(msgId)
    pahoMsg.setQos(1)

    pubilsh(
      GATEWAY_STATISTICS_TOPIC,
      pahoMsg,
      (token:IMqttToken) =>  {
        logger.warn(s"Gateway statistic event is reported to ThingPlug. statistic=${data.toString()}")
        statistic.telemetryUploaded.set(0)
        statistic.attributesUploaded.set(0)
      },
      (token:IMqttToken, e:Throwable) =>  logger.error(s"Failed to report gateway statistic event.e=${e.getLocalizedMessage}")
    )
  }

  def pubilsh(topic:String, pahoMsg:PahoMqttMessage, onSuccessUserFunc: IMqttToken => Unit, onFailureUserFunc: (IMqttToken, Throwable)  => Unit): Future[IMqttToken] = {
    try {
      val p = Promise[IMqttToken]
      client.publish(topic, pahoMsg, null, new IMqttActionListener() {
        def onSuccess(asyncActionToken: IMqttToken): Unit = {
          onSuccessUserFunc(asyncActionToken)
          p.success(asyncActionToken)
        }

        def onFailure(asyncActionToken: IMqttToken, e: Throwable): Unit = {
          onFailureUserFunc(asyncActionToken, e)
          p.failure(e)
        }
      })

      p.future
    }
    catch {
      case e: Throwable =>
        logger.error(s"Failed to report gw event. e=${e.getLocalizedMessage}")
        Future.failed(e)
    }
  }
}
