package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class TravelForWorkSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "TravelForWork" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(TravelForWork.values.toSeq)

      forAll(gen) {
        travelForWork =>

          JsString(travelForWork.toString).validate[TravelForWork].asOpt.value mustEqual travelForWork
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!TravelForWork.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[TravelForWork] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(TravelForWork.values.toSeq)

      forAll(gen) {
        travelForWork =>

          Json.toJson(travelForWork) mustEqual JsString(travelForWork.toString)
      }
    }
  }
}
