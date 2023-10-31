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

package forms

import forms.behaviours.OptionFieldBehaviours
import forms.expenses.DisallowableFeesFormProvider
import models.disallowableFees
import play.api.data.FormError

class disallowableFeesFormProviderSpec extends OptionFieldBehaviours {

  val form = new DisallowableFeesFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "disallowableFees.error.required"

    behave like optionsField[disallowableFees](
      form,
      fieldName,
      validValues  = disallowableFees.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
