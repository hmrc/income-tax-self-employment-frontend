package forms

import forms.behaviours.OptionFieldBehaviours
import forms.expenses.EntertainmentCostsFormProvider
import models.EntertainmentCosts
import play.api.data.FormError

class EntertainmentCostsFormProviderSpec extends OptionFieldBehaviours {

  val form = new EntertainmentCostsFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "entertainmentCosts.error.required"

    behave like optionsField[EntertainmentCosts](
      form,
      fieldName,
      validValues  = EntertainmentCosts.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
