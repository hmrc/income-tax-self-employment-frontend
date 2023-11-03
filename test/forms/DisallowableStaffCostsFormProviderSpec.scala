package forms

import forms.behaviours.OptionFieldBehaviours
import forms.expenses.DisallowableStaffCostsFormProvider
import models.DisallowableStaffCosts
import play.api.data.FormError

class DisallowableStaffCostsFormProviderSpec extends OptionFieldBehaviours {

  val form = new DisallowableStaffCostsFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "disallowableStaffCosts.error.required"

    behave like optionsField[DisallowableStaffCosts](
      form,
      fieldName,
      validValues  = DisallowableStaffCosts.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
