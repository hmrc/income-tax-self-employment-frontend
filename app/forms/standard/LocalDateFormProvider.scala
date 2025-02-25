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

import forms.mappings.Mappings
import models.common.UserType
import pages.OneQuestionPage
import play.api.data.Form

import java.time.LocalDate
import javax.inject.{Inject, Singleton}

@Singleton
class LocalDateFormProvider @Inject() extends Mappings {

  def apply(page: OneQuestionPage[_],
            userType: UserType,
            userSpecificRequiredError: Boolean = false,
            earliestDateAndError: Option[(LocalDate, String)] = None,
            latestDateAndError: Option[(LocalDate, String)] = None): Form[LocalDate] = {

    val pageName      = page.pageName.value
    val requiredError = s"$pageName.error.required${if (userSpecificRequiredError) s".$userType" else ""}"

    Form(
      pageName -> localDate(
        requiredKey = requiredError,
        earliestDateAndError = earliestDateAndError,
        latestDateAndError = latestDateAndError
      ).verifying("error.date.valid", date => date.getYear.toString.matches("\\d{4}"))
    )
  }
}
