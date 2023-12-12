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

import base.SpecBase
import forms.behaviours.BigDecimalFieldBehaviours
import models.common.UserType.{Agent, Individual}
import play.api.data.FormError

class NotTaxableAmountFormProviderSpec extends BigDecimalFieldBehaviours with SpecBase {

  ".value" - {

    val fieldName = "value"

    val userTypes = Seq(Individual, Agent)

    userTypes.foreach { user =>
      val form = new NotTaxableAmountFormProvider()(user)

      s"when user is an $user, form should " - {

        val validDataGenerator = bigDecimalsInRangeWithCommas(zeroValue, maxAmountValue)

        behave like fieldThatBindsValidData(
          form,
          fieldName,
          validDataGenerator
        )

        behave like bigDecimalField(
          form,
          fieldName,
          nonNumericError = FormError(fieldName, s"notTaxableAmount.error.nonNumeric.$user")
        )

        behave like bigDecimalFieldWithMinimum(
          form,
          fieldName,
          zeroValue,
          expectedError = FormError(fieldName, s"notTaxableAmount.error.lessThanZero.$user", Seq(zeroValue))
        )

        behave like bigDecimalFieldWithMaximum(
          form,
          fieldName,
          maxAmountValue,
          expectedError = FormError(fieldName, s"notTaxableAmount.error.overMax.$user", Seq(maxAmountValue))
        )

        behave like mandatoryField(
          form,
          fieldName,
          requiredError = FormError(fieldName, s"notTaxableAmount.error.required.$user")
        )
      }
    }
  }

}
