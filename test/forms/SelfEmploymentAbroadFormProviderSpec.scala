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

import forms.abroad.SelfEmploymentAbroadFormProvider
import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class SelfEmploymentAbroadFormProviderSpec extends BooleanFieldBehaviours {

  ".value" - {

    val fieldName  = "value"
    val invalidKey = "error.boolean"
    val isAgent    = false

    def requiredKey(isAgent: Boolean) = s"selfEmploymentAbroad.error.required.${if (isAgent) "agent" else "individual"}"

    def form(isAgent: Boolean) = new SelfEmploymentAbroadFormProvider()(isAgent)

    behave like booleanField(
      form(isAgent),
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form(isAgent),
      fieldName,
      requiredError = FormError(fieldName, requiredKey(false))
    )
  }

}
