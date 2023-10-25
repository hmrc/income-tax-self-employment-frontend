package models.expenses

import models.journeys.GoodsToSellOrUse
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class GoodsToSellOrUseSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "GoodsToSellOrUse" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(GoodsToSellOrUse.values.toSeq)

      forAll(gen) { goodsToSellOrUse =>
        JsString(goodsToSellOrUse.toString).validate[GoodsToSellOrUse].asOpt.value mustEqual goodsToSellOrUse
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!GoodsToSellOrUse.values.map(_.toString).contains(_))

      forAll(gen) { invalidValue =>
        JsString(invalidValue).validate[GoodsToSellOrUse] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(GoodsToSellOrUse.values.toSeq)

      forAll(gen) { goodsToSellOrUse =>
        Json.toJson(goodsToSellOrUse) mustEqual JsString(goodsToSellOrUse.toString)
      }
    }
  }

}
