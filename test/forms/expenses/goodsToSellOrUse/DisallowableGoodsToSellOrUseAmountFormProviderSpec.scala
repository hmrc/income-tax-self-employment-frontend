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

package forms.expenses.goodsToSellOrUse

import forms.behaviours.BigDecimalFieldBehaviours
import models.common.UserType
import models.common.UserType.{Agent, Individual}
import play.api.data.FormError
import utils.MoneyUtils.formatMoney

class DisallowableGoodsToSellOrUseAmountFormProviderSpec extends BigDecimalFieldBehaviours {

  ".value" - {

    val fieldName         = "value"
    val minimum           = 0
    val goodsAmount       = 5623.50
    val goodsAmountString = formatMoney(goodsAmount, false)

    case class UserScenario(userType: UserType)

    val userScenarios = Seq(UserScenario(Individual), UserScenario(Agent))

    userScenarios.foreach { userScenario =>
      val form = new DisallowableGoodsToSellOrUseAmountFormProvider()(userScenario.userType, goodsAmount)

      s"when user is an ${userScenario.userType}, form should " - {

        val validDataGenerator = currencyInRangeWithCommas(minimum, goodsAmount)

        behave like fieldThatBindsValidData(
          form,
          fieldName,
          validDataGenerator
        )

        behave like bigDecimalField(
          form,
          fieldName,
          nonNumericError =
            FormError(fieldName, s"disallowableGoodsToSellOrUseAmount.error.nonNumeric.${userScenario.userType}", Seq(goodsAmountString))
        )

        behave like bigDecimalFieldWithMinimum(
          form,
          fieldName,
          minimum,
          expectedError =
            FormError(fieldName, s"disallowableGoodsToSellOrUseAmount.error.lessThanZero.${userScenario.userType}", Seq(goodsAmountString))
        )

        behave like bigDecimalFieldWithMaximum(
          form,
          fieldName,
          goodsAmount,
          expectedError = FormError(fieldName, s"disallowableGoodsToSellOrUseAmount.error.overMax.${userScenario.userType}", Seq(goodsAmountString))
        )

        behave like mandatoryField(
          form,
          fieldName,
          requiredError = FormError(fieldName, s"disallowableGoodsToSellOrUseAmount.error.required.${userScenario.userType}", Seq(goodsAmountString))
        )
      }
    }
  }

}
