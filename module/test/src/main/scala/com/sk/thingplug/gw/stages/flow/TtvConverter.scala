package com.sk.thingplug.gw.stages.flow

import java.nio.{ByteBuffer, ByteOrder}

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.clients.consumer.ConsumerRecord

/**
  * Created by Cho on 2017-06-21.
  */
class TtvConverter[T] extends GraphStage[FlowShape[T, Array[Map[String, Any]]]] with StrictLogging {
  val in = Inlet[T]("TtvConverter.in")
  val out = Outlet[Array[Map[String, Any]]]("TtvConverter.out")
  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    setHandler(in, new InHandler {
      override def onPush() = grab(in) match {
        case record: ConsumerRecord[Array[Byte], String] =>
          if (TtvConverter.Validate(record.value)) push(out, TtvConverter.parseTtv(record.value))
          else pull(in)
        case _ => logger.error("Not supported type")
      }
    })
    setHandler(out, new OutHandler {
      override def onPull = pull(in)
    })
  }
}

object TtvConverter extends StrictLogging {
  private val identifier = "SKTP"
  private val typeLength = 2
  private val dataTypeLength =2

  /** ttv data convert example **/
  // SKTP060100                 : Array(Map("type" -> "publicaccess", "dataType" -> "boolean", "value" -> false))
  // SKTP030a420e6666           : Array(Map("type" -> "temperature", "dataType" -> "float", "value" -> 35.6))
  // SKTP060100030a420e6666 :
  // Array(
  //  Map("type" -> "publicaccess","dataType" -> "boolean","value" -> false)
  //  Map("type" -> "temperature","dataType" -> "float", "value" -> 35.6)
  // )
  def parseTtv(ttvString: String) = {
    var parsedArray = Array.empty[Map[String, Any]]
    var parsedData = ""

    var ttvHex = ttvString.substring(identifier.length)

    while(ttvHex.length > 0)
    {
      val typeHex = ttvHex.substring(0, 2)
      val dataTypeHex = ttvHex.substring(2, 4)

      val resultType = Type.matchWithString(typeHex)
      val resultDataType = DataType.matchWithString(dataTypeHex)

      val currentTtvLength = typeLength + dataTypeLength + resultDataType.length
      val valueHex = ttvHex.substring(typeLength + dataTypeLength, currentTtvLength)

      var map = Map.empty[String, Any]
      map += ("type" -> resultType.toString.toLowerCase)
      map += ("dataType" -> resultDataType.toString.toLowerCase.substring(3))

      val value = resultDataType match {
        case TtvBoolean => toBoolean(valueHex)
        //        case TtvChar           =>
        //        case TtvUnsignedChar   =>
        //        case TtvShort          =>
        //        case TtvUnsignedShort  =>
        //        case TtvInt            =>
        //        case TtvUnsignedInt    =>
        //        case TtvLong           =>
        //        case TtvUnsignedLong   =>
        case TtvFloat => toFloat(valueHex)
        //        case TtvDouble         =>
        //        case TtvMacAddress     =>
        //        case TtvTime           =>
        //        case TtvChar12         =>
        //        case TtvPreciseTime    =>
        //        case TtvChar2          =>
        //        case TtvChar3          =>
        //        case TtvChar4          =>
        //        case TtvChar8          =>
        //        case TtvChar16         =>
        //        case TtvChar32         =>
        //        case TtvChar64         =>
        //        case TtvChar128        =>
        //        case TtvLatitude       =>
        //        case TtvLongitude      =>
      }

      map += ("value" -> value)

      import org.json4s._
      import org.json4s.native.Serialization._
      import org.json4s.native.Serialization
      implicit val formats = Serialization.formats(NoTypeHints)

      logger.info("Converting completed : " + write(map))
      parsedArray :+= map

      ttvHex = ttvHex.substring(currentTtvLength)
    }
    parsedArray
  }

  def Validate(ttvString: String): Boolean = {
      if (ttvString.isEmpty || !ttvString.startsWith(identifier)) {
        logger.error("Invalid ttv data"); false
    }
    else {
      var ttvHex = ttvString.substring(identifier.length)

      if (ttvHex.length < typeLength || ttvHex.length < typeLength + dataTypeLength) {
        logger.error("Invalid ttv data");
        return false
      }

      while (ttvHex.length > 0) {
        val typeHex = ttvHex.substring(0, 2)
        val dataTypeHex = ttvHex.substring(2, 4)

        val resultDataType = DataType.matchWithString(dataTypeHex)

        if (resultDataType == null) return false

        val currentTtvLength = typeLength + dataTypeLength + resultDataType.length

        if (currentTtvLength > ttvHex.length) return false

        val valueHex = ttvHex.substring(typeLength + dataTypeLength, currentTtvLength)

        if (Type.matchWithString(typeHex) == null) {
          logger.error("Not supported type");
          return false
        }
        else if (DataType.matchWithString(dataTypeHex) == null) {
          logger.error("Not supported datatype");
          return false
        }
        else if (valueHex.isEmpty) {
          logger.error("Invalid ttv data");
          return false
        }
        else if (valueHex.length != DataType.matchWithString(dataTypeHex).length) {
          logger.error("Invalid ttv data");
          return false
        }
        else {
          ttvHex = ttvHex.substring(currentTtvLength)
        }
      }
      true
    }
  }

  private def toBoolean(hex: String) = {
    if (!"00".equals(hex)) true
    else false
  }

  private def toFloat(hex:String) = {
    val byteArray = new java.math.BigInteger(hex, 16).toByteArray
    ByteBuffer.wrap(byteArray).order(ByteOrder.BIG_ENDIAN).getFloat
  }
}

/**
  * Type
  */
sealed abstract class Type

case object DeviceToken   extends Type
case object MessageType   extends Type
case object Temperature   extends Type
case object Longitude     extends Type
case object Latitude      extends Type
case object PublicAccess  extends Type

object Type {
  private val DEVICE_TOKEN  = "01"
  private val MESSAGE_TYPE  = "02"
  private val TEMPERATURE   = "03"
  private val LONGITUDE     = "04"
  private val LATITUDE      = "05"
  private val PUBLIC_ACCESS = "06"


  def matchWithString(hex: String): Type = hex match {
    case DEVICE_TOKEN   => DeviceToken
    case MESSAGE_TYPE   => MessageType
    case TEMPERATURE    => Temperature
    case LONGITUDE      => Longitude
    case LATITUDE       => Latitude
    case PUBLIC_ACCESS  => PublicAccess
    case _              => null
  }
}

/**
  * DataType
  */
sealed abstract class DataType { def length: Int }

case object TtvBoolean        extends DataType {  val length = 2   }
case object TtvChar           extends DataType {  val length = 2   }
case object TtvUnsignedChar   extends DataType {  val length = 2   }
case object TtvShort          extends DataType {  val length = 4   }
case object TtvUnsignedShort  extends DataType {  val length = 4   }
case object TtvInt            extends DataType {  val length = 8   }
case object TtvUnsignedInt    extends DataType {  val length = 8   }
case object TtvLong           extends DataType {  val length = 8   }
case object TtvUnsignedLong   extends DataType {  val length = 8   }
case object TtvFloat          extends DataType {  val length = 8   }
case object TtvDouble         extends DataType {  val length = 16  }
//case object TtvMacAddress     extends DataType {  val length = ??? }
case object TtvTime           extends DataType {  val length = 8   }
case object TtvChar12         extends DataType {  val length = 24  }
case object TtvPreciseTime    extends DataType {  val length = 16  }
case object TtvChar2          extends DataType {  val length = 4   }
case object TtvChar3          extends DataType {  val length = 6   }
case object TtvChar4          extends DataType {  val length = 8   }
case object TtvChar8          extends DataType {  val length = 16  }
case object TtvChar16         extends DataType {  val length = 32  }
case object TtvChar32         extends DataType {  val length = 64  }
case object TtvChar64         extends DataType {  val length = 128 }
case object TtvChar128        extends DataType {  val length = 256 }
case object TtvLatitude       extends DataType {  val length = 8   }
case object TtvLongitude      extends DataType {  val length = 8   }

object DataType {
  private val BOOLEAN         = "01"
  private val CHAR            = "02"
  private val UNSIGNED_CHAR   = "03"
  private val SHORT           = "04"
  private val UNSIGNED_SHORT  = "05"
  private val INT             = "06"
  private val UNSIGNED_INT    = "07"
  private val LONG            = "08"
  private val UNSIGNED_LONG   = "09"
  private val FLOAT           = "0a"
  private val DOUBLE          = "0b"
  //  private val MAC_ADDRESS     = "0c"
  private val TIME            = "0d"
  private val CHAR_12         = "0e"
  private val PRECISE_TIME    = "0f"
  private val CHAR_2          = "10"
  private val CHAR_3          = "11"
  private val CHAR_4          = "12"
  private val CHAR_8          = "13"
  private val CHAR_16         = "14"
  private val CHAR_32         = "15"
  private val CHAR_64         = "16"
  private val CHAR_128        = "17"
  private val LATITUDE        = "18"
  private val LONGITUDE       = "19"

  def matchWithString(hex: String) = hex match {
    case BOOLEAN        => TtvBoolean
    case CHAR           => TtvChar
    case UNSIGNED_CHAR  => TtvUnsignedChar
    case SHORT          => TtvShort
    case UNSIGNED_SHORT => TtvUnsignedShort
    case INT            => TtvInt
    case UNSIGNED_INT   => TtvUnsignedInt
    case LONG           => TtvLong
    case UNSIGNED_LONG  => TtvUnsignedLong
    case FLOAT          => TtvFloat
    case DOUBLE         => TtvDouble
    //    case MAC_ADDRESS    => TtvMacAddress
    case TIME           => TtvTime
    case CHAR_12        => TtvChar12
    case PRECISE_TIME   => TtvPreciseTime
    case CHAR_2         => TtvChar2
    case CHAR_3         => TtvChar3
    case CHAR_4         => TtvChar4
    case CHAR_8         => TtvChar8
    case CHAR_16        => TtvChar16
    case CHAR_32        => TtvChar32
    case CHAR_64        => TtvChar64
    case CHAR_128       => TtvChar128
    case LATITUDE       => TtvLatitude
    case LONGITUDE      => TtvLongitude
    case _              => null
  }
}