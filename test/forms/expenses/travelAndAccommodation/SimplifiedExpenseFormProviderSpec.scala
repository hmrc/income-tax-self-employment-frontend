/*
 * Copyright 2025 HM Revenue & Customs
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

package forms.expenses.travelAndAccommodation

import forms.behaviours.BooleanFieldBehaviours
import models.common.UserType
import play.api.data.FormError

class SimplifiedExpenseFormProviderSpec extends BooleanFieldBehaviours {

  val vehicle: String     = "vehicle"
  val userType: UserType  = UserType.Individual
  val requiredKey: String = s"simplifiedExpenses.error.required.$userType"
  val invalidKey: String  = "error.boolean"

  val form = new SimplifiedExpenseFormProvider()(UserType.Individual, vehicle)

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey, Seq(vehicle))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(vehicle))
    )
  }
}
