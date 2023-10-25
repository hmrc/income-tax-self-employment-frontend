package models.expenses

import models.journeys.OfficeSupplies
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class OfficeSuppliesSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "OfficeSupplies" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(OfficeSupplies.values.toSeq)

      forAll(gen) { officeSupplies =>
        JsString(officeSupplies.toString).validate[OfficeSupplies].asOpt.value mustEqual officeSupplies
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!OfficeSupplies.values.map(_.toString).contains(_))

      forAll(gen) { invalidValue =>
        JsString(invalidValue).validate[OfficeSupplies] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(OfficeSupplies.values.toSeq)

      forAll(gen) { officeSupplies =>
        Json.toJson(officeSupplies) mustEqual JsString(officeSupplies.toString)
      }
    }
  }

}
