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

import forms.behaviours.BigDecimalFieldBehaviours
import models.common.UserType
import play.api.data.FormError

import scala.collection.immutable.ArraySeq

class VehicleExpensesFormProviderSpec extends BigDecimalFieldBehaviours {

  ".value" - {

    val fieldName = "value"
    val minimum   = 0
    val maximum   = 100000000000.00

    case class UserScenario(userType: UserType)

    val userScenarios = Seq(UserScenario(UserType.Individual), UserScenario(UserType.Agent))

    userScenarios.foreach { userScenario =>
      val form = new VehicleExpensesFormProvider()(userScenario.userType)

      s"when user is an ${userScenario.userType}, form should " - {

        val validDataGenerator = currencyInRangeWithCommas(minimum, maximum)

        behave like fieldThatBindsValidData(
          form,
          fieldName,
          validDataGenerator
        )

        behave like bigDecimalField(
          form,
          fieldName,
          nonNumericError = FormError(fieldName, s"vehicleExpenses.error.nonNumeric.${userScenario.userType}")
        )

        behave like bigDecimalFieldWithMinimum(
          form,
          fieldName,
          minimum,
          expectedError = FormError(fieldName, s"vehicleExpenses.error.lessThanZero.${userScenario.userType}", ArraySeq(minimum))
        )

        behave like bigDecimalFieldWithMaximum(
          form,
          fieldName,
          maximum,
          expectedError = FormError(fieldName, s"vehicleExpenses.error.overMax.${userScenario.userType}", ArraySeq(maximum))
        )

        behave like mandatoryField(
          form,
          fieldName,
          requiredError = FormError(fieldName, s"vehicleExpenses.error.required.${userScenario.userType}")
        )
      }
    }
  }
}
