package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class TravelForWorkYourVehicleFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("travelForWorkYourVehicle.error.required")
        .verifying(maxLength(100, "travelForWorkYourVehicle.error.length"))
    )
}
