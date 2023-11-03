package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class DisallowableSubcontractorCostsSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "DisallowableSubcontractorCosts" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(DisallowableSubcontractorCosts.values.toSeq)

      forAll(gen) {
        disallowableSubcontractorCosts =>

          JsString(disallowableSubcontractorCosts.toString).validate[DisallowableSubcontractorCosts].asOpt.value mustEqual disallowableSubcontractorCosts
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!DisallowableSubcontractorCosts.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[DisallowableSubcontractorCosts] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(DisallowableSubcontractorCosts.values.toSeq)

      forAll(gen) {
        disallowableSubcontractorCosts =>

          Json.toJson(disallowableSubcontractorCosts) mustEqual JsString(disallowableSubcontractorCosts.toString)
      }
    }
  }
}
