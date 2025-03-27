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

package forms

import forms.behaviours.OptionFieldBehaviours
import forms.expenses.travelAndAccommodation.YourFlatRateForVehicleExpensesFormProvider
import models.common.UserType
import models.journeys.expenses.travelAndAccommodation.YourFlatRateForVehicleExpenses
import play.api.data.FormError
class YourFlatRateForVehicleExpensesFormProviderSpec extends OptionFieldBehaviours {

  val mileage: BigDecimal   = 200
  val totalFlatRate: String = "90.00"

  ".value" - {
    UserType.values foreach { userType =>
      s"for the userType $userType" - {
        val form = new YourFlatRateForVehicleExpensesFormProvider()(mileage, userType)

        val fieldName   = "value"
        val requiredKey = s"yourFlatRateForVehicleExpenses.error.required.$userType"

        behave like optionsField[YourFlatRateForVehicleExpenses](
          form,
          fieldName,
          validValues = YourFlatRateForVehicleExpenses.values,
          invalidError = FormError(fieldName, "error.invalid", Seq(totalFlatRate))
        )

        behave like mandatoryField(
          form,
          fieldName,
          requiredError = FormError(fieldName, requiredKey, Seq(totalFlatRate))
        )
      }
    }

  }
}
