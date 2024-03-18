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

package forms.capitalallowances.specialTaxSites

import forms.{DateBaseFormProvider, DateFormModel, ValidDateError}
import play.api.data.{Form, FormError}

import java.time.LocalDate

object ConstructionStartDateFormProvider extends DateBaseFormProvider {

  private val earliestDate      = LocalDate.of(2018, 10, 29)
  private val requiredError     = "constructionStartDate.error"
  private val dateTooEarlyError = "constructionStartDate.error.tooEarly"

  val formProvider = createForm(earliestDate, dateTooEarlyError)

  def checkForWholeFormErrors(errorForm: Form[DateFormModel]): (Form[DateFormModel], Boolean) =
    if (allFieldsEmpty(errorForm)) (errorForm.discardingErrors.withError(FormError("", requiredError)), true)
    else if (invalidEntry(errorForm)) (errorForm.discardingErrors.withError(FormError("", ValidDateError)), true)
    else if (dateTooEarly(errorForm, earliestDate)) (errorForm.discardingErrors.withError(FormError("", dateTooEarlyError)), true)
    else (errorForm, false)

}
