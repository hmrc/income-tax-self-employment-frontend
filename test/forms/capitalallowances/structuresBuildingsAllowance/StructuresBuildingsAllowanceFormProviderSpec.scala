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

package forms.capitalallowances.structuresBuildingsAllowance

import base.forms.BooleanFormProviderBaseSpec
import models.common.UserType
import play.api.data.Form

class StructuresBuildingsAllowanceFormProviderSpec
    extends BooleanFormProviderBaseSpec(
      "StructuresBuildingsAllowanceFormProvider"
    ) {

  override def formProvider(userType: UserType): Form[Boolean] = new StructuresBuildingsAllowanceFormProvider()(userType)

  override def requiredErrorKey: String = "structuresBuildingsAllowance.error.required"

}
