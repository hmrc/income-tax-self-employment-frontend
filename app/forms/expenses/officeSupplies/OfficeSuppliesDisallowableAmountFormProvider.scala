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

package forms.expenses.officeSupplies

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class OfficeSuppliesDisallowableAmountFormProvider @Inject() extends Mappings {

  def apply(authUserType: String, allowableAmount: BigDecimal): Form[BigDecimal] =
    Form(
      "value" -> bigDecimal(
        s"officeSuppliesDisallowableAmount.error.required.$authUserType",
        s"officeSuppliesDisallowableAmount.error.nonNumeric.$authUserType")
        .verifying(greaterThan(BigDecimal(0), s"officeSuppliesDisallowableAmount.error.lessThanZero.$authUserType"))
        .verifying(lessThan(BigDecimal(100000000000.00), s"officeSuppliesDisallowableAmount.error.overMax.$authUserType"))
        .verifying(maximumValue(allowableAmount, s"officeSuppliesDisallowableAmount.error.overAllowableMax.$authUserType"))
    )

}
