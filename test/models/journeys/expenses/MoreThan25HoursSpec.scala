package models.journeys.expenses

import models.journeys.expenses.workplaceRunningCosts.workingFromHome.MoreThan25Hours
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class MoreThan25HoursSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "MoreThan25Hours" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(MoreThan25Hours.values.toSeq)

      forAll(gen) { MoreThan25Hours =>
        JsString(MoreThan25Hours.toString).validate[MoreThan25Hours].asOpt.value mustEqual MoreThan25Hours
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!MoreThan25Hours.values.map(_.toString).contains(_))

      forAll(gen) { invalidValue =>
        JsString(invalidValue).validate[MoreThan25Hours] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(MoreThan25Hours.values.toSeq)

      forAll(gen) { MoreThan25Hours =>
        Json.toJson(MoreThan25Hours) mustEqual JsString(MoreThan25Hours.toString)
      }
    }
  }

}
