package models.expenses

import models.journeys.TaxiMinicabOrRoadHaulage
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class TaxiMinicabOrRoadHaulageSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "TaxiMinicabOrRoadHaulage" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(TaxiMinicabOrRoadHaulage.values.toSeq)

      forAll(gen) { taxiMinicabOrRoadHaulage =>
        JsString(taxiMinicabOrRoadHaulage.toString).validate[TaxiMinicabOrRoadHaulage].asOpt.value mustEqual taxiMinicabOrRoadHaulage
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!TaxiMinicabOrRoadHaulage.values.map(_.toString).contains(_))

      forAll(gen) { invalidValue =>
        JsString(invalidValue).validate[TaxiMinicabOrRoadHaulage] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(TaxiMinicabOrRoadHaulage.values.toSeq)

      forAll(gen) { taxiMinicabOrRoadHaulage =>
        Json.toJson(taxiMinicabOrRoadHaulage) mustEqual JsString(taxiMinicabOrRoadHaulage.toString)
      }
    }
  }

}
