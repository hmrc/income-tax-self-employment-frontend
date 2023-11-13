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

import javax.inject.Inject
import play.api.data.Form

class RepairsAndMaintenanceDisallowableAmountFormProvider @Inject() extends Mappings with MoneyBounds {

  def apply(authUserType: UserType, allowableAmount: BigDecimal): Form[BigDecimal] =
    Form(
      "value" -> bigDecimal(
        s"repairsAndMaintenanceDisallowableAmount.error.required.$authUserType",
        s"repairsAndMaintenanceDisallowableAmount.error.nonNumeric.$authUserType")
        .verifying(greaterThan(minimumValue, s"repairsAndMaintenanceDisallowableAmount.error.lessThanZero.$authUserType"))
        .verifying(lessThan(maximumValue, s"repairsAndMaintenanceDisallowableAmount.error.overMax.$authUserType"))
        .verifying(maximumValue(allowableAmount, s"repairsAndMaintenanceDisallowableAmount.error.overAllowableMax.$authUserType"))

    )

}
