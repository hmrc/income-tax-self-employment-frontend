package forms.$journeyName$

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class $className$FormProvider @Inject() extends Mappings {

  def apply(userType: UserType): Form[BigDecimal] =
    Form(
      "value" -> currency(s"$className;format="decap"$.error.required.\$userType", s"$className;format="decap"$.error.nonNumeric.\$userType")
          .verifying(isBigDecimalGreaterThanZero(s"$className;format="decap"$.error.lessThanZero.\$userType"))
          .verifying(isBigDecimalLessThanMax(BigDecimal("$maximum$"), s"$className;format="decap"$.error.overMax.\$userType"))


  )
}
