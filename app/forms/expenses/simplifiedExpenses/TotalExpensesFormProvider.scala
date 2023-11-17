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

package forms.expenses.simplifiedExpenses

import forms.mappings.Mappings
import models.common.MoneyBounds
import play.api.data.Form

import javax.inject.Inject

class TotalExpensesFormProvider @Inject() extends Mappings with MoneyBounds {

  def apply(userType: String): Form[BigDecimal] =
    Form(
      "value" -> bigDecimal(s"totalExpenses.error.required.$userType", s"totalExpenses.error.nonNumeric.$userType")
        .verifying(greaterThan(minimumValue, s"totalExpenses.error.lessThanZero.$userType"))
        .verifying(lessThan(maximumValue, s"totalExpenses.error.overMax.$userType"))
    )
}
