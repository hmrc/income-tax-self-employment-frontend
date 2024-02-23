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

package forms.capitalallowances.zeroEmissionCars

import base.forms.BooleanFormProviderBaseSpec
import models.common.UserType

class ZeroEmissionCarsFormProviderSpec extends BooleanFormProviderBaseSpec("ZecUsedForWorkForm") {

  override def requiredErrorKey = "zecUsedForWork.error.required"
  override def invalidKeyArgs   = Seq(taxYear.startYear.toString, taxYear.endYear.toString)

  override def formProvider(user: UserType) = new ZeroEmissionCarsFormProvider()(user, taxYear)
}
