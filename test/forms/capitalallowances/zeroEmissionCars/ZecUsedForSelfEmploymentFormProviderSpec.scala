package forms.capitalallowances.zeroEmissionCars

import base.forms.BooleanFormProviderBaseSpec
import models.common.UserType


class ZecUsedForSelfEmploymentFormProviderSpec extends BooleanFormProviderBaseSpec("ZecUsedForSelfEmploymentForm") {

  override def requiredErrorKey = "zecUsedForSelfEmployment.error.required"
  override def formProvider(user: UserType) = new ZecUsedForSelfEmploymentFormProvider()(user)
}

