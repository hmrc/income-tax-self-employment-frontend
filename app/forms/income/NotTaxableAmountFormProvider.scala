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

import forms.mappings.Mappings
import models.common.MoneyBounds
import play.api.data.Form

import javax.inject.Inject

class NotTaxableAmountFormProvider @Inject() extends Mappings with MoneyBounds {

  def apply(authUserType: String, turnoverAmount: BigDecimal): Form[BigDecimal] =
    Form(
      "value" -> bigDecimal(s"notTaxableAmount.error.required.$authUserType", s"notTaxableAmount.error.nonNumeric.$authUserType")
        .verifying(greaterThan(minimumValue, s"notTaxableAmount.error.lessThanZero.$authUserType"))
        .verifying(maximumValue(turnoverAmount, s"notTaxableAmount.error.overTurnover.$authUserType"))
    )

}
