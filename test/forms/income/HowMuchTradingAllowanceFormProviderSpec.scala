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

package forms.income

import forms.behaviours.OptionFieldBehaviours
import models.common.UserType
import models.journeys.income.HowMuchTradingAllowance
import play.api.data.FormError

class HowMuchTradingAllowanceFormProviderSpec extends OptionFieldBehaviours {

  ".value" - {

    val fieldName      = "value"
    val turnoverAmount = "1000.00"
    case class UserScenario(user: UserType)

    val userScenarios = Seq(UserScenario(UserType.Individual), UserScenario(UserType.Agent))

    userScenarios.foreach { userScenario =>
      val form = new HowMuchTradingAllowanceFormProvider()(userScenario.user, turnoverAmount)

      s"when user is an ${userScenario.user}, form should " - {

        behave like optionsField[HowMuchTradingAllowance](
          form,
          fieldName,
          validValues = HowMuchTradingAllowance.values,
          invalidError = FormError(fieldName, "error.invalid", Seq(turnoverAmount))
        )

        behave like mandatoryField(
          form,
          fieldName,
          requiredError = FormError(fieldName, s"howMuchTradingAllowance.error.required.${userScenario.user}", Seq(turnoverAmount))
        )

      }
    }
  }

}
