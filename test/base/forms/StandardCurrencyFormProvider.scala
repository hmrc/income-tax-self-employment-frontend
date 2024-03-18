package base.forms

import forms.behaviours.SimpleFieldBehaviours
import forms.mappings.Mappings.userTypeAware
import models.common.UserType
import play.api.data.Form

class StandardCurrencyFormProvider(errorPrefix: String, mkForm: UserType => Form[BigDecimal], validNumber: BigDecimal = 10.0)
    extends SimpleFieldBehaviours {

  "BigDecimal Form" - forAllUserTypes { userType =>
    implicit val form = mkForm(userType)

    "bind valid values" - {
      checkValidValue(validNumber)
    }

    "not bind invalid values" - {
      checkMandatoryField(userTypeAware(userType, s"$errorPrefix.error.required"))
      checkLessThanField()
      checkNonNumericField()
      checkMaximumField()
      checkRegexp()
    }
  }
}
