package forms

import forms.behaviours.BooleanFieldBehaviours
import models.common.UserType
import models.common.UserType.{Agent, Individual}
import play.api.data.FormError

class FarmerOrMarketGardenerFormProviderSpec extends BooleanFieldBehaviours {

  private val formProvider = new FarmerOrMarketGardenerFormProvider()

  private def form(userType: UserType) = formProvider(userType)

  ".apply" - {

    "for an Individual user" - {
      val userType = Individual
      val testForm = form(userType)

      "should bind true correctly" in {
        testForm.bind(Map("value" -> "true")).value mustBe Some(true)
      }

      "should bind false correctly" in {
        testForm.bind(Map("value" -> "false")).value mustBe Some(false)
      }

      "should return the correct error for empty value" in {
        val result = testForm.bind(Map("value" -> ""))
        result.errors must contain only FormError("value", s"farmerOrMarketGardener.error.required.$userType")
      }

      "should return the correct error for invalid value" in {
        val result = testForm.bind(Map("value" -> "invalid"))
        result.errors must contain only FormError("value", "error.boolean")
      }
    }

    "for an Agent user" - {
      val userType = Agent
      val testForm = form(userType)

      "should bind true correctly" in {
        testForm.bind(Map("value" -> "true")).value mustBe Some(true)
      }

      "should bind false correctly" in {
        testForm.bind(Map("value" -> "false")).value mustBe Some(false)
      }

      "should return the correct error for empty value" in {
        val result = testForm.bind(Map("value" -> ""))
        result.errors must contain only FormError("value", s"farmerOrMarketGardener.error.required.$userType")
      }

      "should return the correct error for invalid value" in {
        val result = testForm.bind(Map("value" -> "invalid"))
        result.errors must contain only FormError("value", "error.boolean")
      }
    }
  }

  ".value" - {
    val fieldName = "value"

    Seq(Individual, Agent).foreach { userType =>
      s"for $userType" - {
        behave like booleanField(
          form(userType),
          fieldName,
          invalidError = FormError(fieldName, "error.boolean")
        )

        behave like mandatoryField(
          form(userType),
          fieldName,
          requiredError = FormError(fieldName, s"farmerOrMarketGardener.error.required.$userType")
        )
      }
    }
  }
}