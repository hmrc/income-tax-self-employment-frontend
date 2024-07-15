/*
 * Copyright 2024 HM Revenue & Customs
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

package forms.standard

import forms.mappings.Mappings._
import forms.{LessThanZeroError, NonNumericError, OverMaxError}
import models.common.{AccountingType, MoneyBounds, UserType}
import pages.OneQuestionPage
import play.api.data.Form

import javax.inject.{Inject, Singleton}

@Singleton
class CurrencyFormProvider @Inject() {

  def apply(page: OneQuestionPage[BigDecimal],
            userType: UserType,
            minValue: BigDecimal = MoneyBounds.minimumValue,
            minValueError: String = LessThanZeroError,
            maxValue: BigDecimal = MoneyBounds.maximumValue,
            maxValueError: String = OverMaxError,
            nonNumericError: String = NonNumericError,
            prefix: Option[String] = None,
            optAccountingType: Option[AccountingType] = None): Form[BigDecimal] = {

    def optCashSuffix(prefix: String) = optAccountingTypeAware(optAccountingType)(prefix)
    val requiredError: String = userTypeAware(userType, prefix.fold(optCashSuffix(page.requiredErrorKey))(s => optCashSuffix(s"$s.error.required")))

    val minError: String       = prefix.fold(minValueError)(s => s"$s.error.lessThanZero.$userType")
    val maxError: String       = prefix.fold(maxValueError)(s => s"$s.error.overMax.$userType")
    val nonNumberError: String = prefix.fold(nonNumericError)(s => s"$s.error.nonNumeric.$userType")

    Form(
      "value" -> currency(requiredError, nonNumberError)
        .verifying(greaterThan(minValue, minError))
        .verifying(lessThan(maxValue, maxError))
    )
  }

}
