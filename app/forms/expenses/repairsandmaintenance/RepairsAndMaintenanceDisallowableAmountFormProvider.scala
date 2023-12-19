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

package forms.expenses.repairsandmaintenance

import forms.mappings.Mappings
import models.common.{MoneyBounds, UserType}
import play.api.data.Form
import play.api.i18n.Messages
import utils.MoneyUtils.formatMoney

import javax.inject.Inject

class RepairsAndMaintenanceDisallowableAmountFormProvider @Inject() extends Mappings with MoneyBounds {

  def apply(userType: UserType, allowableAmount: BigDecimal)(implicit messages: Messages): Form[BigDecimal] = {
    val formattedMoney = formatMoney(allowableAmount)

    Form(
      "value" -> currency(
        messages(s"repairsAndMaintenanceDisallowableAmount.error.required.$userType", formattedMoney),
        messages(s"repairsAndMaintenanceDisallowableAmount.error.nonNumeric.$userType", formattedMoney)
      )
        .verifying(greaterThan(minimumValue, messages(s"repairsAndMaintenanceDisallowableAmount.error.lessThanZero.$userType", formattedMoney)))
        .verifying(maximumValue(allowableAmount, messages(s"repairsAndMaintenanceDisallowableAmount.error.overAmount.$userType", formattedMoney)))
    )
  }

}
