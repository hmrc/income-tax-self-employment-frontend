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

package forms.expenses

import forms.behaviours.OptionFieldBehaviours
import models.journeys.expenses.DisallowableOtherFinancialCharges
import play.api.data.FormError

class DisallowableOtherFinancialChargesFormProviderSpec extends OptionFieldBehaviours {

  val form = new DisallowableOtherFinancialChargesFormProvider()()

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "disallowableOtherFinancialCharges.error.required"

    behave like optionsField[DisallowableOtherFinancialCharges](
      form,
      fieldName,
      validValues = DisallowableOtherFinancialCharges.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

}
