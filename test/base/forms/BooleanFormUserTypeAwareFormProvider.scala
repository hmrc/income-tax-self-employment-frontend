package base.forms

import forms.behaviours.SimpleFieldBehaviours
import forms.capitalallowances.zeroEmissionCars.ZecUseOutsideSEFormProvider.userTypeAware
import models.common.UserType
import play.api.data.Form

abstract class BooleanFormUserTypeAwareFormProvider(errorPrefix: String, mkForm: UserType => Form[Boolean]) extends SimpleFieldBehaviours {

  "WdaSpecialRateFormProvider" - forAllUserTypes { userType =>
    implicit val form = mkForm(userType)

    "bind valid values" - {
      checkValidBoolean()
    }

    "not bind invalid values" - {
      checkMandatoryField(userTypeAware(userType, s"$errorPrefix.error.required"))
    }
  }
}
