package forms.base

import forms.capitalallowances.zeroEmissionCars.ZecUseOutsideSEFormProvider.userTypeAware
import forms.mappings.Mappings._
import models.common.UserType
import pages.OneQuestionPage
import play.api.data.Form

abstract class BooleanFormProvider(page: OneQuestionPage[_]) {
  def apply(userType: UserType): Form[Boolean] =
    Form("value" -> boolean(s"${userTypeAware(userType, page.requiredErrorKey)}"))

}
