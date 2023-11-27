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

package forms.expenses.staffCosts

import forms.mappings.Mappings
import models.common.MoneyBounds
import play.api.data.Form
import utils.MoneyUtils

import javax.inject.Inject

class StaffCostsDisallowableAmountFormProvider @Inject() extends Mappings with MoneyBounds with MoneyUtils {

  def apply(userType: String, staffCosts: BigDecimal): Form[BigDecimal] = {

    val staffCostsString = formatMoney(staffCosts)
    Form(
      "value" -> bigDecimal(
        s"staffCostsDisallowableAmount.error.required.$userType",
        s"staffCostsDisallowableAmount.error.nonNumeric.$userType",
        Seq(staffCostsString))
        .verifying(greaterThan(minimumValue, s"staffCostsDisallowableAmount.error.lessThanZero.$userType", Some(staffCostsString)))
        .verifying(lessThanOrEqualTo(staffCosts, s"staffCostsDisallowableAmount.error.overMax.$userType", Some(staffCostsString)))
    )
  }

}