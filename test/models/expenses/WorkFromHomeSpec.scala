package models.expenses

import models.journeys.WorkFromHome
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class WorkFromHomeSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "WorkFromHome" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(WorkFromHome.values.toSeq)

      forAll(gen) { workFromHome =>
        JsString(workFromHome.toString).validate[WorkFromHome].asOpt.value mustEqual workFromHome
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!WorkFromHome.values.map(_.toString).contains(_))

      forAll(gen) { invalidValue =>
        JsString(invalidValue).validate[WorkFromHome] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(WorkFromHome.values.toSeq)

      forAll(gen) { workFromHome =>
        Json.toJson(workFromHome) mustEqual JsString(workFromHome.toString)
      }
    }
  }

}
