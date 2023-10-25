package forms.expenses

import forms.behaviours.OptionFieldBehaviours
import models.journeys.TaxiMinicabOrRoadHaulage
import play.api.data.FormError

class TaxiMinicabOrRoadHaulageFormProviderSpec extends OptionFieldBehaviours {

  val form = new TaxiMinicabOrRoadHaulageFormProvider()()

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "taxiMinicabOrRoadHaulage.error.required"

    behave like optionsField[TaxiMinicabOrRoadHaulage](
      form,
      fieldName,
      validValues = TaxiMinicabOrRoadHaulage.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

}
