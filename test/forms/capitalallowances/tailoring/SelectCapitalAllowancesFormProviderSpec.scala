/*
 * Copyright 2024 HM Revenue & Customs
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

package forms.capitalallowances.tailoring

import forms.behaviours.CheckboxFieldBehaviours
import models.common.UserType
import models.common.UserType.{Agent, Individual}
import models.journeys.capitalallowances.tailoring.CapitalAllowances
import models.journeys.capitalallowances.tailoring.CapitalAllowances.{allAccrualAllowances, allCashAllowances}
import play.api.data.FormError

class SelectCapitalAllowancesFormProviderSpec extends CheckboxFieldBehaviours {

  val userTypes: List[UserType] = List(Individual, Agent)

  userTypes.foreach { user =>
    val form = new SelectCapitalAllowancesFormProvider()(user)

    val fieldName   = "value"
    val requiredKey = s"selectCapitalAllowances.error.required.$user"

    s"when user = $user, form should " - {
      behave like checkboxField[CapitalAllowances](
        form,
        fieldName,
        validValues = (allCashAllowances ++ allAccrualAllowances).distinct,
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
