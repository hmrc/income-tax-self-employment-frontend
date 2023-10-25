package models.expenses

import models.journeys.RepairsAndMaintenance
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class RepairsAndMaintenanceSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "RepairsAndMaintenance" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(RepairsAndMaintenance.values.toSeq)

      forAll(gen) { repairsAndMaintenance =>
        JsString(repairsAndMaintenance.toString).validate[RepairsAndMaintenance].asOpt.value mustEqual repairsAndMaintenance
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!RepairsAndMaintenance.values.map(_.toString).contains(_))

      forAll(gen) { invalidValue =>
        JsString(invalidValue).validate[RepairsAndMaintenance] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(RepairsAndMaintenance.values.toSeq)

      forAll(gen) { repairsAndMaintenance =>
        Json.toJson(repairsAndMaintenance) mustEqual JsString(repairsAndMaintenance.toString)
      }
    }
  }

}
