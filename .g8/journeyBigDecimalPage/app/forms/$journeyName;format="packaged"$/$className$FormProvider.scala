package forms.$journeyName$

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class $className$FormProvider @Inject() extends Mappings {

  def apply(authUserType: String): Form[BigDecimal] =
    Form(
      "value" -> bigDecimal(s"$className;format="decap"$.error.required.\$authUserType", s"$className;format="decap"$.error.nonNumeric.\$authUserType")
          .verifying(isBigDecimalGreaterThanZero(s"$className;format="decap"$.error.lessThanZero.\$authUserType"))
          .verifying(isBigDecimalLessThanMax(BigDecimal("$maximum$"), s"$className;format="decap"$.error.overMax.\$authUserType"))


  )
}
