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

package forms.expenses.irrecoverableDebts

import forms.mappings.Mappings
import models.common.{MoneyBounds, UserType}
import play.api.data.Form
import utils.MoneyUtils

import javax.inject.Inject

class IrrecoverableDebtsDisallowableAmountFormProvider @Inject() extends Mappings with MoneyBounds with MoneyUtils {

  def apply(userType: UserType, allowableAmount: BigDecimal): Form[BigDecimal] = {

    val allowableAmountString = formatMoney(allowableAmount)
    Form(
      "value" -> currency(
        s"irrecoverableDebtsDisallowableAmount.error.required.$userType",
        s"irrecoverableDebtsDisallowableAmount.error.nonNumeric.$userType",
        Seq(allowableAmountString)
      )
        .verifying(greaterThan(minimumValue, s"irrecoverableDebtsDisallowableAmount.error.lessThanZero.$userType", Some(allowableAmountString)))
        .verifying(
          lessThanOrEqualTo(allowableAmount, s"irrecoverableDebtsDisallowableAmount.error.overAmount.$userType", Some(allowableAmountString)))
    )
  }
}
