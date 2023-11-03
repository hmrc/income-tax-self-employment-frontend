package forms

import forms.behaviours.OptionFieldBehaviours
import forms.expenses.DisallowableProfessionalFeesFormProvider
import models.DisallowableProfessionalFees
import play.api.data.FormError

class DisallowableProfessionalFeesFormProviderSpec extends OptionFieldBehaviours {

  val form = new DisallowableProfessionalFeesFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "disallowableProfessionalFees.error.required"

    behave like optionsField[DisallowableProfessionalFees](
      form,
      fieldName,
      validValues  = DisallowableProfessionalFees.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
