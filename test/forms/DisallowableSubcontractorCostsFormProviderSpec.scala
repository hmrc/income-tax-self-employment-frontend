package forms

import forms.behaviours.OptionFieldBehaviours
import forms.expenses.DisallowableSubcontractorCostsFormProvider
import models.DisallowableSubcontractorCosts
import play.api.data.FormError

class DisallowableSubcontractorCostsFormProviderSpec extends OptionFieldBehaviours {

  val form = new DisallowableSubcontractorCostsFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "disallowableSubcontractorCosts.error.required"

    behave like optionsField[DisallowableSubcontractorCosts](
      form,
      fieldName,
      validValues  = DisallowableSubcontractorCosts.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
