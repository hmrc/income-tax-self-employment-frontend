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

import forms.behaviours.SimpleFieldBehaviours
import forms.capitalallowances.zeroEmissionCars.ZecUseOutsideSEFormProvider.userTypeAware
import models.common.UserType
import play.api.data.Form

abstract class StandardBooleanFormProviderSpec(errorPrefix: String, mkForm: UserType => Form[Boolean]) extends SimpleFieldBehaviours {

  "Boolean Form" - forAllUserTypes { userType =>
    implicit val form = mkForm(userType)

    "bind valid values" - {
      checkValidBoolean()
    }

    "not bind invalid values" - {
      checkMandatoryField(userTypeAware(userType, s"$errorPrefix.error.required"))
    }
  }
}
