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

package forms.expenses

import forms.behaviours.CheckboxFieldBehaviours
import models.journeys.expenses.ProfessionalServiceExpenses
import play.api.data.FormError

class ProfessionalServiceExpensesFormProviderSpec extends CheckboxFieldBehaviours {

  ".value" - {
    case class UserScenario(user: String)

    val userScenarios = Seq(UserScenario(individual), UserScenario(agent))

    userScenarios.foreach { userScenario =>
      val form = new ProfessionalServiceExpensesFormProvider()(userScenario.user)

      val fieldName = "value"
      val requiredKey = s"professionalServiceExpenses.error.required"

      s"when user is an ${userScenario.user}, form should " - {

        behave like checkboxField[ProfessionalServiceExpenses](
          form,
          fieldName,
          validValues = ProfessionalServiceExpenses.values,
          invalidError = FormError(s"$fieldName[0]", "error.invalid")
        )

        behave like mandatoryCheckboxField(
          form,
          fieldName,
          requiredKey
        )
      }
    }
  }

}
