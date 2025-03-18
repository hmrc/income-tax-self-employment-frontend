/*
 * Copyright 2025 HM Revenue & Customs
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

package forms.expenses.travelAndAccommodation

import base.SpecBase
import forms.behaviours.CheckboxFieldBehaviours
import models.common.UserType
import models.journeys.expenses.travelAndAccommodation.TravelAndAccommodationExpenseType
import play.api.data.FormError

class TravelAndAccommodationFormProviderSpec extends CheckboxFieldBehaviours with SpecBase {

  val individualForm = new TravelAndAccommodationFormProvider()(UserType.Individual)
  val agentForm      = new TravelAndAccommodationFormProvider()(UserType.Agent)

  "TravelAndAccommodationFormProvider individual" - {

    "validate individual travel and accommodation expense type" - {
      val fieldName   = "value"
      val requiredKey = "travelAndAccommodationExpenseType.error.required.individual"

      behave like checkboxField[TravelAndAccommodationExpenseType](
        individualForm,
        fieldName,
        validValues = TravelAndAccommodationExpenseType.values,
        invalidError = FormError(s"$fieldName[0]", "error.invalid")
      )

      behave like mandatoryCheckboxField(
        individualForm,
        fieldName,
        requiredKey
      )
    }

  }

  "TravelAndAccommodationFormProvider agent" - {

    "validate agent travel and accommodation expense type" - {
      val fieldName   = "value"
      val requiredKey = "travelAndAccommodationExpenseType.error.required.agent"

      behave like checkboxField[TravelAndAccommodationExpenseType](
        agentForm,
        fieldName,
        validValues = TravelAndAccommodationExpenseType.values,
        invalidError = FormError(s"$fieldName[0]", "error.invalid")
      )

      behave like mandatoryCheckboxField(
        agentForm,
        fieldName,
        requiredKey
      )
    }

  }
}
