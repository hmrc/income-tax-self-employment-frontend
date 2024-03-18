package forms.base

import forms.mappings.Mappings._
import forms.{LessThanZeroError, NoDecimalsError, NonNumericError, OverMaxError}
import models.common.{MoneyBounds, UserType}
import pages.OneQuestionPage
import play.api.data.Form

abstract class CurrencyFormProvider(page: OneQuestionPage[_],
                                    minValue: BigDecimal = MoneyBounds.minimumValue,
                                    maxValue: BigDecimal = MoneyBounds.maximumValue) {
  def apply(userType: UserType): Form[BigDecimal] =
    Form(
      "value" -> currency(userTypeAware(userType, page.requiredErrorKey), NonNumericError)
        .verifying(greaterThan(minValue, LessThanZeroError))
        .verifying(lessThan(maxValue, OverMaxError))
        .verifying(regexpBigDecimal(MoneyBounds.noDecimalRegexp, NoDecimalsError))
    )

}
