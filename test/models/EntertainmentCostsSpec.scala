package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class EntertainmentCostsSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "EntertainmentCosts" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(EntertainmentCosts.values.toSeq)

      forAll(gen) {
        entertainmentCosts =>

          JsString(entertainmentCosts.toString).validate[EntertainmentCosts].asOpt.value mustEqual entertainmentCosts
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!EntertainmentCosts.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[EntertainmentCosts] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(EntertainmentCosts.values.toSeq)

      forAll(gen) {
        entertainmentCosts =>

          Json.toJson(entertainmentCosts) mustEqual JsString(entertainmentCosts.toString)
      }
    }
  }
}
