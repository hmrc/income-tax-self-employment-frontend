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
import models.common.UserType
import play.api.data.FormError

class StaffCostsAmountFormProviderSpec extends BigDecimalFieldBehaviours {

  val formProvider = new StaffCostsAmountFormProvider()

  ".value" - {

    val fieldName = "value"
    val minimum   = BigDecimal("0")
    val maximum   = BigDecimal("100000000000.00")

    val users = List(UserType.Individual, UserType.Agent)

    users.foreach { user =>
      val form = formProvider(user)

      s"when user is an $user, form should " - {

        val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, maximum)

        behave like fieldThatBindsValidData(
          form,
          fieldName,
          validDataGenerator
        )

        behave like bigDecimalField(
          form,
          fieldName,
          nonNumericError = FormError(fieldName, s"staffCostsAmount.error.nonNumeric.$user")
        )

        behave like bigDecimalFieldWithMinimum(
          form,
          fieldName,
          minimum = minimum,
          expectedError = FormError(fieldName, s"staffCostsAmount.error.lessThanZero.$user", Seq(minimum))
        )

        behave like bigDecimalFieldWithMaximum(
          form,
          fieldName,
          maximum,
          expectedError = FormError(fieldName, s"staffCostsAmount.error.overMax.$user", Seq(maximum))
        )

        behave like mandatoryField(
          form,
          fieldName,
          requiredError = FormError(fieldName, s"staffCostsAmount.error.required.$user")
        )
      }
    }
  }
}
