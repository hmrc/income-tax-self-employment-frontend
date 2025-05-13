package forms.abroad

import forms.behaviours.BooleanFieldBehaviours
import forms.industrysectors.LiteraryOrCreativeWorksFormProvider
import models.common.UserType.{Agent, Individual}
import play.api.data.FormError

class LiteraryOrCreativeWorksFormProviderSpec extends BooleanFieldBehaviours {

  val invalidKey = "error.boolean"

  Seq(Individual, Agent) foreach { userType =>
    s".value for the userType $userType" - {

      val form      = new LiteraryOrCreativeWorksFormProvider()(userType)
      val fieldName = "value"

      behave like booleanField(
        form,
        fieldName,
        invalidError = FormError(fieldName, invalidKey)
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, s"literaryOrCreativeWorks.error.required.$userType")
      )
    }
  }

}
