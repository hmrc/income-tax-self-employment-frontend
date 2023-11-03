package models

import generators.ModelGenerators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class ProfessionalServiceExpensesSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues with ModelGenerators {

  "ProfessionalServiceExpenses" - {

    "must deserialise valid values" in {

      val gen = arbitrary[ProfessionalServiceExpenses]

      forAll(gen) {
        professionalServiceExpenses =>

          JsString(professionalServiceExpenses.toString).validate[ProfessionalServiceExpenses].asOpt.value mustEqual professionalServiceExpenses
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!ProfessionalServiceExpenses.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[ProfessionalServiceExpenses] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = arbitrary[ProfessionalServiceExpenses]

      forAll(gen) {
        professionalServiceExpenses =>

          Json.toJson(professionalServiceExpenses) mustEqual JsString(professionalServiceExpenses.toString)
      }
    }
  }
}
