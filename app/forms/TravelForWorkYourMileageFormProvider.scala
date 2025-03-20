package forms

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class TravelForWorkYourMileageFormProvider @Inject() extends Mappings {

  def apply(): Form[Int] =
    Form(
      "value" -> int(
        "travelForWorkYourMileage.error.required",
        "travelForWorkYourMileage.error.wholeNumber",
        "travelForWorkYourMileage.error.nonNumeric")
          .verifying(inRange(0, Int.MaxValue, "travelForWorkYourMileage.error.outOfRange"))
    )
}
