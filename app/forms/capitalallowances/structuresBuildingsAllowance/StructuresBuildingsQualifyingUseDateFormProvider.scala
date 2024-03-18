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

package forms.capitalallowances.structuresBuildingsAllowance

import forms.{DateBaseFormProvider, DateFormModel, ValidDateError}
import play.api.data.{Form, FormError}

import java.time.LocalDate

object StructuresBuildingsQualifyingUseDateFormProvider extends DateBaseFormProvider {

  private val latestDate        = LocalDate.now()
  private val requiredError     = "structuresBuildingsQualifyingUseDate.error"
  private val dateInFutureError = "structuresBuildingsQualifyingUseDate.error.inFuture"

  val formProvider = createForm(latestDate, dateInFutureError)

  def checkForWholeFormErrors(errorForm: Form[DateFormModel]): (Form[DateFormModel], Boolean) =
    if (allFieldsEmpty(errorForm)) (errorForm.discardingErrors.withError(FormError("", requiredError)), true)
    else if (invalidEntry(errorForm)) (errorForm.discardingErrors.withError(FormError("", ValidDateError)), true)
    else if (dateInFuture(errorForm, latestDate)) (errorForm.discardingErrors.withError(FormError("", dateInFutureError)), true)
    else (errorForm, false)
}
