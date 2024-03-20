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

import forms.behaviours.SimpleFieldBehaviours
import pages.capitalallowances.specialTaxSites.ConstructionStartDatePage
import play.api.data.{Form, FormError}

import java.time.LocalDate

class LocalDateFormProviderSpec extends SimpleFieldBehaviours {

  private val fieldKey: String     = "constructionStartDate"
  private val earliestDate         = LocalDate.of(2000, 10, 29)
  private val earlyDateError       = "constructionStartDate.error.tooEarly"
  private val latestDate           = LocalDate.of(3000, 10, 29)
  private val lateDateError        = "constructionStartDate.error.tooLate"
  private val earliestDateAndError = Some((earliestDate, earlyDateError))
  private val latestDateAndError   = Some((latestDate, lateDateError))
  private val validDates           = datesBetween(earliestDate, latestDate)

  "LocalDate Form should" - forAllUserTypes { userType =>
    val form: Form[LocalDate] =
      new LocalDateFormProvider()(ConstructionStartDatePage, userType, true, earliestDateAndError, latestDateAndError)
    val requiredError = s"constructionStartDate.error.required.$userType"

    dateField(form, fieldKey, validDates)

    mandatoryDateField(form, fieldKey, requiredError)

    dateFieldWithMin(form, fieldKey, earliestDate, FormError(fieldKey, earlyDateError))

    dateFieldWithMax(form, fieldKey, latestDate, FormError(fieldKey, lateDateError))
  }
}
