package module.kafka

import java.math.BigInteger
import java.nio.{ByteBuffer, ByteOrder}

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.google.common.primitives.Ints
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * Created by kwonkr01 on 2017-06-21.
  */
class TtvConvertTest extends TestKit(ActorSystem("TtvTest"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with ScalaFutures{

  "ttv test" should {
    "ttv to boolean" in {
      val ttvRawData = "SKTP010100"

      parseTtv(ttvRawData) shouldBe false
    }

    "ttv to unsigned int" in {
      val ttvRawData = "SKTP0107FFFFFE0C"

      parseTtv(ttvRawData) shouldBe -500
    }

    "ttv to float" in {
      val ttvRawData = "SKTP010a4129999a"

      parseTtv(ttvRawData) shouldBe 10.6f
    }

    "ttv to char" in {
      val ttvRawData = "SKTP011254727565"

      parseTtv(ttvRawData) shouldBe "True"
    }
  }

  def parseTtv(data: String) = {
    val ttvData = data.replace("SKTP", "")
    val ttvType = ttvData.substring(0, 2)
    val ttvDataType = ttvData.substring(2, 4)
    val ttvValue = ttvData.substring(4)

    ttvDataType match {
      case DataType.FLOAT => toFloatNumber(ttvValue)
      case DataType.UNSIGNED_INT => toIntNumber(ttvValue)
      case DataType.CHAR_4 => toAscii(ttvValue)
      case DataType.BOOLEAN => toBoolean(ttvValue)
      case _=> ""
    }
  }

  def toAscii(hex: String) = {
    val sb = new StringBuilder
    for (i <- 0 until hex.size by 2) {
      val str = hex.substring(i, i + 2)
      sb.append(Integer.parseInt(str, 16).toChar)
    }
    sb.toString
  }

  def toIntNumber(number: String) = {
    val byteArray = new java.math.BigInteger(number, 16).toByteArray
    Ints.fromByteArray(byteArray)
    new BigInteger(byteArray).intValue
  }

  def toFloatNumber(number: String) = {
    val byteArray = new java.math.BigInteger(number, 16).toByteArray
    ByteBuffer.wrap(byteArray).order(ByteOrder.BIG_ENDIAN).getFloat
  }

  def toBoolean(boolean: String) = {
    var booleanValue = false
    if (!"00".equals(boolean))
      booleanValue = true
    booleanValue
  }
}

object DataType {
  val BOOLEAN = "01"
  val CHAR = "02"
  val UNSIGNED_CHAR = "03"
  val SHORT = "04"
  val UNSIGNED_SHORT = "05"
  val INT = "06"
  val UNSIGNED_INT = "07"
  val LONG = "08"
  val UNSIGNED_LONG = "09"
  val FLOAT = "0a"
  val DOUBLE = "0b"
  val MAC_ADDRESS = "0c"
  val TIME = "0d"
  val CHAR_12 = "0e"
  val PRECISE_TIME = "0f"
  val CHAR_2 = "10"
  val CHAR_3 = "11"
  val CHAR_4 = "12"
  val CHAR_8 = "13"
  val CHAR_16 = "14"
  val CHAR_32 = "15"
  val CHAR_64 = "16"
  val CHAR_128 = "17"
  val LATITUDE = "18"
  val LONGITUDE = "19"
}
