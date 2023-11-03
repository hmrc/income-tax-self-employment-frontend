package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class DisallowableProfessionalFeesSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "DisallowableProfessionalFees" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(DisallowableProfessionalFees.values.toSeq)

      forAll(gen) {
        disallowableProfessionalFees =>

          JsString(disallowableProfessionalFees.toString).validate[DisallowableProfessionalFees].asOpt.value mustEqual disallowableProfessionalFees
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!DisallowableProfessionalFees.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[DisallowableProfessionalFees] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(DisallowableProfessionalFees.values.toSeq)

      forAll(gen) {
        disallowableProfessionalFees =>

          Json.toJson(disallowableProfessionalFees) mustEqual JsString(disallowableProfessionalFees.toString)
      }
    }
  }
}
