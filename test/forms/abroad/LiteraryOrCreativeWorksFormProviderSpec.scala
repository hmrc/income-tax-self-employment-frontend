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

package forms.abroad

import forms.behaviours.BooleanFieldBehaviours
import forms.industrysectors.LiteraryOrCreativeWorksFormProvider
import models.common.UserType.{Agent, Individual}
import play.api.data.FormError

class LiteraryOrCreativeWorksFormProviderSpec extends BooleanFieldBehaviours {

  val invalidKey = "error.boolean"

  Seq(Individual, Agent) foreach { userType =>
    s".value for the userType $userType" - {

      val form      = new LiteraryOrCreativeWorksFormProvider()(userType)
      val fieldName = "value"

      behave like booleanField(
        form,
        fieldName,
        invalidError = FormError(fieldName, invalidKey)
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, s"literaryOrCreativeWorks.error.required.$userType")
      )
    }
  }

}
