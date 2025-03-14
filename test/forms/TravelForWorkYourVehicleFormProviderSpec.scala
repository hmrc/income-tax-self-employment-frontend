package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class TravelForWorkYourVehicleFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "travelForWorkYourVehicle.error.required"
  val lengthKey = "travelForWorkYourVehicle.error.length"
  val maxLength = 100

  val form = new TravelForWorkYourVehicleFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
