package forms

import forms.behaviours.OptionFieldBehaviours
import forms.expenses.AdvertisingOrMarketingFormProvider
import models.AdvertisingOrMarketing
import play.api.data.FormError

class AdvertisingOrMarketingFormProviderSpec extends OptionFieldBehaviours {

  val form = new AdvertisingOrMarketingFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "advertisingOrMarketing.error.required"

    behave like optionsField[AdvertisingOrMarketing](
      form,
      fieldName,
      validValues  = AdvertisingOrMarketing.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
