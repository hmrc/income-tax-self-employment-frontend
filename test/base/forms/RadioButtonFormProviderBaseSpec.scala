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

package base.forms

import forms.behaviours.OptionFieldBehaviours
import models.common.UserType
import models.common.UserType.{Agent, Individual}
import play.api.data.{Form, FormError}

abstract case class RadioButtonFormProviderBaseSpec[T](formProviderName: String) extends OptionFieldBehaviours {

  private val fieldName = "value"

  private val userTypes: List[UserType] = List(Individual, Agent)

  val validValues: Seq[T]
  val requiredError: String

  def getFormProvider(userType: UserType): Form[T]

  userTypes.foreach { userType =>
    s"$formProviderName for $userType, form should" - {
      val form: Form[T] = getFormProvider(userType)

      behave like optionsField(
        form,
        fieldName,
        validValues,
        invalidError = FormError(fieldName, "error.invalid")
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, s"$requiredError.$userType")
      )
    }
  }
}
