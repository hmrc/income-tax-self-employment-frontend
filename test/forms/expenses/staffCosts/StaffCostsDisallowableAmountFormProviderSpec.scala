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

import forms.behaviours.BigDecimalFieldBehaviours
import models.common.UserType.{Agent, Individual}
import play.api.data.FormError
import utils.MoneyUtils.formatMoney

class StaffCostsDisallowableAmountFormProviderSpec extends BigDecimalFieldBehaviours {

  ".value" - {

    val fieldName        = "value"
    val minimum          = 0
    val staffCosts       = 5623.50
    val staffCostsString = formatMoney(staffCosts)

    val users = List(Individual, Agent)

    users.foreach { user =>
      val form = new StaffCostsDisallowableAmountFormProvider()(user, staffCosts)

      s"when user is an $user, form should " - {

        val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, staffCosts)

        behave like fieldThatBindsValidData(
          form,
          fieldName,
          validDataGenerator
        )

        behave like bigDecimalField(
          form,
          fieldName,
          nonNumericError = FormError(fieldName, s"staffCostsDisallowableAmount.error.nonNumeric.$user", Seq(staffCostsString))
        )

        behave like bigDecimalFieldWithMinimum(
          form,
          fieldName,
          minimum,
          expectedError = FormError(fieldName, s"staffCostsDisallowableAmount.error.lessThanZero.$user", Seq(staffCostsString))
        )

        behave like bigDecimalFieldWithMaximum(
          form,
          fieldName,
          staffCosts,
          expectedError = FormError(fieldName, s"staffCostsDisallowableAmount.error.overMax.$user", Seq(staffCostsString))
        )

        behave like mandatoryField(
          form,
          fieldName,
          requiredError = FormError(fieldName, s"staffCostsDisallowableAmount.error.required.$user", Seq(staffCostsString))
        )
      }
    }
  }
}
