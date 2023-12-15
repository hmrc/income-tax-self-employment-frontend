/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package forms

import forms.abroad.SelfEmploymentAbroadFormProvider
import forms.behaviours.BooleanFieldBehaviours
import models.common.UserType
import models.common.UserType.{Agent, Individual}
import play.api.data.FormError

class SelfEmploymentAbroadFormProviderSpec extends BooleanFieldBehaviours {

  ".value" - {

    val fieldName  = "value"
    val invalidKey = "error.boolean"
    case class UserScenario(userType: UserType)

    val userScenarios = Seq(UserScenario(Individual), UserScenario(Agent))

    userScenarios.foreach { userScenario =>
      val form = new SelfEmploymentAbroadFormProvider()(userScenario.userType)

      s"when user is an ${userScenario.userType}, form should " - {

        behave like booleanField(
          form,
          fieldName,
          invalidError = FormError(fieldName, invalidKey)
        )

        behave like mandatoryField(
          form,
          fieldName,
          requiredError = FormError(fieldName, s"selfEmploymentAbroad.error.required.${userScenario.userType}")
        )
      }
    }

  }

}
