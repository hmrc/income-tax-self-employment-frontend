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

package forms.capitalallowances.balancingAllowance

import forms.behaviours.BigDecimalFieldBehaviours
import models.common.{MoneyBounds, UserType}
import models.common.UserType.Individual
import org.scalacheck.Gen
import play.api.data.{Form, FormError}

class BalancingAllowanceAmountFormProviderSpec extends BigDecimalFieldBehaviours with MoneyBounds {

  val minimumVal: BigDecimal   = minimumValue
  val maximumVal: BigDecimal   = maxAmountValue
  val requiredError: String    = "balancingAllowanceAmount.error.required"
  val nonNumericError: String  = "error.nonNumeric"
  val lessThanMinError: String = "error.lessThanZero"
  val overMaxError: String     = "expenses.error.overMax"

  val userType: UserType = Individual
  val fieldName          = "value"

  val validDataGenerator: Gen[String]    = intsInRangeWithCommas(minimumVal.toInt, maximumVal.toInt)
  val dataDecimalsGenerator: Gen[String] = bigDecimalsInRangeWithCommas(minimumVal, maximumVal)

  "BalancingAllowanceAmountFormProvider should" - {
    val form: Form[BigDecimal] = new BalancingAllowanceAmountFormProvider()(userType)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      bigDecimalsInRangeWithNoDecimals(minimumVal, maximumValue)
    )

    behave like bigDecimalField(
      form,
      fieldName,
      FormError(fieldName, nonNumericError)
    )

    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximumValue,
      FormError(fieldName, overMaxError, Seq(maximumValue))
    )

    behave like mandatoryField(
      form,
      fieldName,
      FormError(fieldName, s"$requiredError.$userType")
    )

  }
}
