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

import forms.behaviours.BigDecimalFieldBehaviours
import play.api.data.FormError

class OfficeSuppliesDisallowableAmountFormProviderSpec extends BigDecimalFieldBehaviours {

  private val authUserTypes = Seq("individual", "agent")

  private val fieldName = "value"

  private val allowableAmount: BigDecimal = BigDecimal.decimal(1000.00)
  private val minimumAmount: BigDecimal   = 0

  private val validDataGenerator = bigDecimalsInRangeWithCommas(minimumAmount, allowableAmount)

  ".value" - {
    authUserTypes.foreach { authUser =>
      s"when the user is $authUser" - {
        "form provider should" - {
          val form = new OfficeSuppliesDisallowableAmountFormProvider()(authUser, allowableAmount)

          behave like fieldThatBindsValidData(
            form,
            fieldName,
            validDataGenerator
          )

          behave like bigDecimalField(
            form,
            fieldName,
            nonNumericError = FormError(fieldName, s"officeSuppliesDisallowableAmount.error.nonNumeric.$authUser")
          )

          behave like bigDecimalFieldWithMinimum(
            form,
            fieldName,
            minimumAmount,
            expectedError = FormError(fieldName, s"officeSuppliesDisallowableAmount.error.lessThanZero.$authUser", Seq(minimumAmount))
          )

          behave like bigDecimalFieldWithMaximum(
            form,
            fieldName,
            allowableAmount,
            expectedError = FormError(fieldName, s"officeSuppliesDisallowableAmount.error.overAllowableMax.$authUser", Seq(allowableAmount))
          )

          behave like mandatoryField(
            form,
            fieldName,
            requiredError = FormError(fieldName, s"officeSuppliesDisallowableAmount.error.required.$authUser")
          )
        }
      }
    }

  }

}