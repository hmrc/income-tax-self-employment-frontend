package forms.expenses

import forms.behaviours.OptionFieldBehaviours
import models.journeys.GoodsToSellOrUse
import play.api.data.FormError

class GoodsToSellOrUseFormProviderSpec extends OptionFieldBehaviours {

  val form = new GoodsToSellOrUseFormProvider()()

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "goodsToSellOrUse.error.required"

    behave like optionsField[GoodsToSellOrUse](
      form,
      fieldName,
      validValues = GoodsToSellOrUse.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

}
