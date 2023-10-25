package forms.expenses

import forms.behaviours.OptionFieldBehaviours
import models.journeys.WorkFromBusinessPremises
import play.api.data.FormError

class WorkFromBusinessPremisesFormProviderSpec extends OptionFieldBehaviours {

  val form = new WorkFromBusinessPremisesFormProvider()()

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "workFromBusinessPremises.error.required"

    behave like optionsField[WorkFromBusinessPremises](
      form,
      fieldName,
      validValues = WorkFromBusinessPremises.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

}
