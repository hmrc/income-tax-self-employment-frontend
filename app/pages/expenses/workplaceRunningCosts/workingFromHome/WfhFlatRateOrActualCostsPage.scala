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

package pages.expenses.workplaceRunningCosts.workingFromHome

import models.common.BusinessId
import models.journeys.expenses.workplaceRunningCosts.WfhFlatRateOrActualCosts
import models.requests.DataRequest
import pages.expenses.workplaceRunningCosts.WorkplaceRunningCostsBasePage

case object WfhFlatRateOrActualCostsPage extends WorkplaceRunningCostsBasePage[WfhFlatRateOrActualCosts] {
  override def toString: String = "wfhFlatRateOrActualCosts"

  def previousPagesAreAnswered(request: DataRequest[_], businessId: BusinessId): Boolean =
    WorkingFromHomeHoursPage.previousPagesAreAnswered(request, businessId)

  def pageAndPreviousAreAnswered(request: DataRequest[_], businessId: BusinessId): Boolean = {
    request.getValue(this, businessId).isDefined && previousPagesAreAnswered(request, businessId)
    p1.getValue().contains(false) || p2.pageAndPreviousPagesAreAnswered
  }
}
