package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class AdvertisingOrMarketingSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "AdvertisingOrMarketing" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(AdvertisingOrMarketing.values.toSeq)

      forAll(gen) {
        advertisingOrMarketing =>

          JsString(advertisingOrMarketing.toString).validate[AdvertisingOrMarketing].asOpt.value mustEqual advertisingOrMarketing
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!AdvertisingOrMarketing.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[AdvertisingOrMarketing] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(AdvertisingOrMarketing.values.toSeq)

      forAll(gen) {
        advertisingOrMarketing =>

          Json.toJson(advertisingOrMarketing) mustEqual JsString(advertisingOrMarketing.toString)
      }
    }
  }
}
