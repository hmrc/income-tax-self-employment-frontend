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

package base.forms

import forms.behaviours.BooleanFieldBehaviours
import models.common.UserType
import models.common.UserType.{Agent, Individual}
import play.api.data.{Form, FormError}

abstract case class BooleanFormProviderBaseSpec(formProviderName: String) extends BooleanFieldBehaviours {

  private val fieldName = "value"

  private val userTypes: List[UserType] = List(Individual, Agent)

  def requiredError: String

  def getFormProvider(userType: UserType): Form[Boolean]

  userTypes.foreach { userType =>
    s"$formProviderName for $userType, form should" - {
      val form: Form[Boolean] = getFormProvider(userType)

      behave like booleanField(
        form,
        fieldName,
        invalidError = FormError(fieldName, "error.boolean")
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, s"$requiredError.$userType")
      )
    }
  }
}
