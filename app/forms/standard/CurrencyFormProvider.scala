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
import models.common.{MoneyBounds, UserType}
import pages.OneQuestionPage
import play.api.data.Form

import javax.inject.{Inject, Singleton}

@Singleton
class CurrencyFormProvider @Inject() {

  def apply(page: OneQuestionPage[_],
            userType: UserType,
            minValue: BigDecimal = MoneyBounds.minimumValue,
            maxValue: BigDecimal = MoneyBounds.maximumValue): Form[BigDecimal] =
    Form(
      "value" -> currency(userTypeAware(userType, page.requiredErrorKey), NonNumericError)
        .verifying(greaterThan(minValue, LessThanZeroError))
        .verifying(lessThan(maxValue, OverMaxError))
    )

}
