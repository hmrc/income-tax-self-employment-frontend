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

import base.forms.RadioButtonFormProviderBaseSpec
import models.common.UserType
import models.journeys.expenses.workplaceRunningCosts.workingFromHome.MoreThan25Hours
import play.api.data.Form

class MoreThan25HoursFormProviderSpec
    extends RadioButtonFormProviderBaseSpec[MoreThan25Hours](
      "MoreThan25HoursFormProvider"
    ) {

  override def getFormProvider(userType: UserType): Form[MoreThan25Hours] = new MoreThan25HoursFormProvider()(userType)

  override lazy val validValues: Seq[MoreThan25Hours] = MoreThan25Hours.values
  override lazy val requiredError: String             = "moreThan25Hours.error.required"

}
