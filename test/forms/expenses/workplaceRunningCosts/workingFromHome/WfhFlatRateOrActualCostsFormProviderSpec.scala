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

package forms.expenses.workplaceRunningCosts.workingFromHome

import base.forms.EnumerableFormProviderBaseSpec
import models.common.UserType
import models.journeys.expenses.workplaceRunningCosts.WfhFlatRateOrActualCosts
import play.api.data.Form

class WfhFlatRateOrActualCostsFormProviderSpec
    extends EnumerableFormProviderBaseSpec[WfhFlatRateOrActualCosts](
      "WfhFlatRateOrActualCostsFormProvider"
    ) {

  override def getFormProvider(userType: UserType): Form[WfhFlatRateOrActualCosts] = new WfhFlatRateOrActualCostsFormProvider()(userType)

  override lazy val validValues: Seq[WfhFlatRateOrActualCosts] = WfhFlatRateOrActualCosts.values
  override lazy val requiredError: String                      = "wfhFlatRateOrActualCosts.error.required"

}
