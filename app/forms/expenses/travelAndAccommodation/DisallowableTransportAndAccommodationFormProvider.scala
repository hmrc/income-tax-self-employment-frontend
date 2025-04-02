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

import forms.mappings.Mappings
import models.common.{MoneyBounds, UserType}
import play.api.data.Form
import utils.MoneyUtils

import javax.inject.Inject

class DisallowableTransportAndAccommodationFormProvider @Inject() extends Mappings with MoneyBounds with MoneyUtils {

  def apply(userType: UserType, allowableAmount: BigDecimal): Form[BigDecimal] = {

    val allowableAmountString = formatMoney(allowableAmount)
    Form(
      "value" -> currency(
        s"disallowableTransportAndAccommodation.error.required.$userType",
        s"disallowableTransportAndAccommodation.error.nonNumeric.$userType",
        Seq(allowableAmountString)
      )
        .verifying(greaterThan(minimumValue, s"disallowableTransportAndAccommodation.error.lessThanZero.$userType", Some(allowableAmountString)))
        .verifying(
          lessThanOrEqualTo(allowableAmount, s"disallowableTransportAndAccommodation.error.overAmount.$userType", Some(allowableAmountString)))
    )
  }
}
