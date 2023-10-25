package forms.expenses

import forms.behaviours.OptionFieldBehaviours
import models.journeys.RepairsAndMaintenance
import play.api.data.FormError

class RepairsAndMaintenanceFormProviderSpec extends OptionFieldBehaviours {

  val form = new RepairsAndMaintenanceFormProvider()()

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "repairsAndMaintenance.error.required"

    behave like optionsField[RepairsAndMaintenance](
      form,
      fieldName,
      validValues = RepairsAndMaintenance.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

}
