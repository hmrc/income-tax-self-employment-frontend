package forms.expenses

import forms.behaviours.OptionFieldBehaviours
import models.journeys.WorkFromHome
import play.api.data.FormError

class WorkFromHomeFormProviderSpec extends OptionFieldBehaviours {

  val form = new WorkFromHomeFormProvider()()

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "workFromHome.error.required"

    behave like optionsField[WorkFromHome](
      form,
      fieldName,
      validValues = WorkFromHome.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

}
