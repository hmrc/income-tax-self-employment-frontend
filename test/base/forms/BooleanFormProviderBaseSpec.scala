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

import base.SpecBase
import forms.behaviours.BooleanFieldBehaviours
import models.common.UserType
import models.common.UserType.{Agent, Individual}
import play.api.data.{Form, FormError}

abstract class BooleanFormProviderBaseSpec(formProvider: String) extends BooleanFieldBehaviours with SpecBase {

  val fieldName = "value"

  val userTypes: List[UserType] = List(Individual, Agent)

  val invalidErrorKey = "error.boolean"

  implicit val messages = messagesStubbed

  def requiredErrorKey: String

  def formProvider(user: UserType): Form[Boolean]

  def invalidKeyArgs: Seq[String] = Seq.empty

  userTypes.foreach { user =>
    s"For user = $user, $formProvider should " - {
      val form = formProvider(user)

      behave like booleanField(
        form,
        fieldName,
        invalidError = FormError(fieldName, invalidErrorKey, args = invalidKeyArgs)
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, s"$requiredErrorKey.$user", args = invalidKeyArgs)
      )
    }
  }
}
