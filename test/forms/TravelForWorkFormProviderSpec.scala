package forms

import forms.behaviours.OptionFieldBehaviours
import forms.expenses.TravelForWorkFormProvider
import models.TravelForWork
import play.api.data.FormError

class TravelForWorkFormProviderSpec extends OptionFieldBehaviours {

  val form = new TravelForWorkFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "travelForWork.error.required"

    behave like optionsField[TravelForWork](
      form,
      fieldName,
      validValues  = TravelForWork.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
