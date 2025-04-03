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

class RemoveVehicleFormProviderSpec extends BooleanFieldBehaviours {

  val invalidKey      = "error.boolean"
  val vehicle: String = "vehicle"

  ".value" - {

    case class UserScenario(userType: UserType)

    val userScenarios = Seq(UserScenario(UserType.Individual), UserScenario(UserType.Agent))

    userScenarios.foreach { userScenario =>
      val form = new RemoveVehicleFormProvider()(userScenario.userType, vehicle)

      s"when user is an ${userScenario.userType}, form should " - {

        val fieldName = "value"

        behave like booleanField(
          form,
          fieldName,
          invalidError = FormError(fieldName, invalidKey, Seq(vehicle))
        )

        behave like mandatoryField(
          form,
          fieldName,
          requiredError = FormError(fieldName, s"removeVehicle.error.required.${userScenario.userType}", Seq(vehicle))
        )
      }
    }
  }
}
