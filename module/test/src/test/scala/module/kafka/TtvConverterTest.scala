package module.kafka

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import com.sk.thingplug.gw.stages.flow.TtvConverter

/**
  * Created by hanyoungtak on 2017. 7. 13..
  */
class TtvConverterTest extends TestKit(ActorSystem("TtvParsingTest"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with ScalaFutures{

  "ttv validate test" should {
    "return false when the input is empty string" in {
      TtvConverter.Validate("") shouldBe false
    }

    "return false when the input is not including identifier" in {
      TtvConverter.Validate("060100") shouldBe false
    }

    "return false when the input is too short identifier" in {
      TtvConverter.Validate("SK060100") shouldBe false
    }

    "return false when the input is too long identifier" in {
      TtvConverter.Validate("SKTPP060100") shouldBe false
    }

    "return false when the input is only identifier without payload" in {
      TtvConverter.Validate("SKTP") shouldBe false
    }

    "return false when the input includes wrong type hex data" in {
      TtvConverter.Validate("SKTPP000100") shouldBe false
    }

    "return false when the input data-type:Boolean but actual data-type:Float" in {
      TtvConverter.Validate("SKTP0601420e6666") shouldBe false
    }

    "return false when the input data-type:Float but actual data-type:Boolean" in {
      TtvConverter.Validate("SKTP030a00") shouldBe false
    }

    "return false when the input is too short (has no type field)" in {
      TtvConverter.Validate("SKTP07FFFFFE0C") shouldBe false
    }

    "return false when the input is too short (has no data-type field)" in {
      TtvConverter.Validate("SKTP01FFFFFE0C") shouldBe false
    }

    "return false when the input is too long" in {
      TtvConverter.Validate("SKTP030a420e666666666") shouldBe false
    }

    "return true when the input is normal TTV Boolean type data" in {
      TtvConverter.Validate("SKTP010100") shouldBe true
    }

    "return true when the input is normal TTV Float type data" in {
      TtvConverter.Validate("SKTP030a420e6666") shouldBe true
    }

    "return false when the parameter includes series full TTV String" in {
      TtvConverter.Validate("SKTP060100SKTP030a420e6666") shouldBe false
    }

    "return true when the parameter includes series TTV String without identifier" in {
      TtvConverter.Validate("SKTP060100030a420e6666") shouldBe true
    }
  }

  "ttv parsing test" should {
    "convert successfully when the input is normal single Boolean TTV data" in {
      val ttvData = TtvConverter.parseTtv("SKTP060100")
      ttvData.length shouldBe 1
      ttvData(0).getOrElse("type", "") shouldBe "publicaccess"
      ttvData(0).getOrElse("dataType", "") shouldBe "boolean"
      ttvData(0).getOrElse("value", "") shouldBe false
    }

    "convert successfully when the input is normal single Float TTV data" in {
      val ttvData = TtvConverter.parseTtv("SKTP030a420e6666")
      ttvData.length shouldBe 1
      ttvData(0).getOrElse("type", "") shouldBe "temperature"
      ttvData(0).getOrElse("dataType", "") shouldBe "float"
      ttvData(0).getOrElse("value", "") shouldBe 35.6f
    }

    "convert successfully when the input is normal series of Boolean Float TTV data" in {
      val ttvData = TtvConverter.parseTtv("SKTP060100030a420e6666")
      ttvData.length shouldBe 2
      ttvData(0).getOrElse("type", "") shouldBe "publicaccess"
      ttvData(0).getOrElse("dataType", "") shouldBe "boolean"
      ttvData(0).getOrElse("value", "") shouldBe false
      ttvData(1).getOrElse("type", "") shouldBe "temperature"
      ttvData(1).getOrElse("dataType", "") shouldBe "float"
      ttvData(1).getOrElse("value", "") shouldBe 35.6f
    }

    "convert successfully when the input is five series of boolean data" in {
      val ttvData = TtvConverter.parseTtv("SKTP060100060100060100060100060100")
      ttvData.length shouldBe 5
      ttvData(0).getOrElse("type", "") shouldBe "publicaccess"
      ttvData(0).getOrElse("dataType", "") shouldBe "boolean"
      ttvData(0).getOrElse("value", "") shouldBe false
      ttvData(1).getOrElse("type", "") shouldBe "publicaccess"
      ttvData(1).getOrElse("dataType", "") shouldBe "boolean"
      ttvData(1).getOrElse("value", "") shouldBe false
      ttvData(2).getOrElse("type", "") shouldBe "publicaccess"
      ttvData(2).getOrElse("dataType", "") shouldBe "boolean"
      ttvData(2).getOrElse("value", "") shouldBe false
      ttvData(3).getOrElse("type", "") shouldBe "publicaccess"
      ttvData(3).getOrElse("dataType", "") shouldBe "boolean"
      ttvData(3).getOrElse("value", "") shouldBe false
      ttvData(4).getOrElse("type", "") shouldBe "publicaccess"
      ttvData(4).getOrElse("dataType", "") shouldBe "boolean"
      ttvData(4).getOrElse("value", "") shouldBe false
    }

    "convert successfully when the input is five series of float data" in {
      val ttvData = TtvConverter.parseTtv("SKTP030a420e6666030a420e6666030a420e6666030a420e6666030a420e6666")
      ttvData.length shouldBe 5
      ttvData(0).getOrElse("type", "") shouldBe "temperature"
      ttvData(0).getOrElse("dataType", "") shouldBe "float"
      ttvData(0).getOrElse("value", "") shouldBe 35.6f
      ttvData(1).getOrElse("type", "") shouldBe "temperature"
      ttvData(1).getOrElse("dataType", "") shouldBe "float"
      ttvData(1).getOrElse("value", "") shouldBe 35.6f
      ttvData(2).getOrElse("type", "") shouldBe "temperature"
      ttvData(2).getOrElse("dataType", "") shouldBe "float"
      ttvData(2).getOrElse("value", "") shouldBe 35.6f
      ttvData(3).getOrElse("type", "") shouldBe "temperature"
      ttvData(3).getOrElse("dataType", "") shouldBe "float"
      ttvData(3).getOrElse("value", "") shouldBe 35.6f
      ttvData(4).getOrElse("type", "") shouldBe "temperature"
      ttvData(4).getOrElse("dataType", "") shouldBe "float"
      ttvData(4).getOrElse("value", "") shouldBe 35.6f

    }
  }
}
