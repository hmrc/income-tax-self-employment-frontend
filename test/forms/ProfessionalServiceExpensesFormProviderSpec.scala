package forms

import forms.behaviours.CheckboxFieldBehaviours
import forms.expenses.ProfessionalServiceExpensesFormProvider
import models.ProfessionalServiceExpenses
import play.api.data.FormError

class ProfessionalServiceExpensesFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new ProfessionalServiceExpensesFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "professionalServiceExpenses.error.required"

    behave like checkboxField[ProfessionalServiceExpenses](
      form,
      fieldName,
      validValues  = ProfessionalServiceExpenses.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}
