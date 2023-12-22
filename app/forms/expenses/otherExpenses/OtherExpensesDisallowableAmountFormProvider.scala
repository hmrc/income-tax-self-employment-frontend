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

package forms.expenses.otherExpenses

import forms.mappings.Mappings
import models.common.{MoneyBounds, UserType}
import play.api.data.Form
import utils.MoneyUtils

import javax.inject.Inject

class OtherExpensesDisallowableAmountFormProvider @Inject() extends Mappings with MoneyBounds with MoneyUtils {

  def apply(user: UserType, totalAmount: BigDecimal): Form[BigDecimal] =
    Form(
      "value" -> currency(
        s"otherExpensesDisallowableAmount.error.required.$user",
        s"otherExpensesDisallowableAmount.error.nonNumeric.$user",
        Seq(formatMoney(totalAmount)))
        .verifying(greaterThan(minimumValue, s"otherExpensesDisallowableAmount.error.lessThanZero.$user", Some(formatMoney(totalAmount))))
        .verifying(lessThanOrEqualTo(totalAmount, s"otherExpensesDisallowableAmount.error.overAllowableMax.$user", Some(formatMoney(totalAmount))))
    )

}
