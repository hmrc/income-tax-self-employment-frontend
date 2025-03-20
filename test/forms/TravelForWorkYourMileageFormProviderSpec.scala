package forms

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class TravelForWorkYourMileageFormProviderSpec extends IntFieldBehaviours {

  val form = new TravelForWorkYourMileageFormProvider()()

  ".value" - {

    val fieldName = "value"

    val minimum = 0
    val maximum = Int.MaxValue

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, "travelForWorkYourMileage.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "travelForWorkYourMileage.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "travelForWorkYourMileage.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "travelForWorkYourMileage.error.required")
    )
  }
}
