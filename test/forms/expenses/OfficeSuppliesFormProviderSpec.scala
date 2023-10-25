package forms.expenses

import forms.behaviours.OptionFieldBehaviours
import models.journeys.OfficeSupplies
import play.api.data.FormError

class OfficeSuppliesFormProviderSpec extends OptionFieldBehaviours {

  val form = new OfficeSuppliesFormProvider()()

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "officeSupplies.error.required"

    behave like optionsField[OfficeSupplies](
      form,
      fieldName,
      validValues = OfficeSupplies.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

}
