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

import forms.behaviours.BigDecimalFieldBehaviours
import models.common.UserType
import play.api.data.FormError

class TradingAllowanceAmountFormProviderSpec extends BigDecimalFieldBehaviours {

  ".value" - {
    val fieldName      = "value"
    val turnoverAmount = 1000.00
    val minimum        = 0
    case class UserScenario(user: UserType)

    val userScenarios = Seq(UserScenario(UserType.Individual), UserScenario(UserType.Agent))

    userScenarios.foreach { userScenario =>
      val form = new TradingAllowanceAmountFormProvider()(userScenario.user, turnoverAmount)

      s"when user is an ${userScenario.user}, form should " - {

        val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, turnoverAmount)

        behave like fieldThatBindsValidData(
          form,
          fieldName,
          validDataGenerator
        )

        behave like bigDecimalField(
          form,
          fieldName,
          nonNumericError = FormError(fieldName, s"tradingAllowanceAmount.error.nonNumeric.${userScenario.user}")
        )

        behave like bigDecimalFieldWithMinimum(
          form,
          fieldName,
          minimum,
          expectedError = FormError(fieldName, s"tradingAllowanceAmount.error.lessThanZero.${userScenario.user}", Seq(minimum))
        )

        behave like bigDecimalFieldWithMaximum(
          form,
          fieldName,
          turnoverAmount,
          expectedError = FormError(fieldName, s"tradingAllowanceAmount.error.overTurnover.${userScenario.user}", Seq(turnoverAmount))
        )

        behave like mandatoryField(
          form,
          fieldName,
          requiredError = FormError(fieldName, s"tradingAllowanceAmount.error.required.${userScenario.user}")
        )
      }
    }
  }

}
