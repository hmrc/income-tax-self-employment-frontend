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

package pages.expenses.workplaceRunningCosts

import controllers.journeys.expenses.workplaceRunningCosts.{routes, workingFromBusinessPremises, workingFromHome}
import models.NormalMode
import models.common.{BusinessId, TaxYear}
import models.requests.DataRequest
import pages.OneQuestionPage
import pages.expenses.tailoring.individualCategories.WorkFromHomePage
import play.api.mvc.Call

trait WorkplaceRunningCostsBasePage[A] extends OneQuestionPage[A] {

  def journeyStartPage(request: DataRequest[_], taxYear: TaxYear, businessId: BusinessId): Call =
    if (request.getValue(WorkFromHomePage, businessId).contains(true))
      workingFromHome.routes.MoreThan25HoursController.onPageLoad(taxYear, businessId, NormalMode)
    else workingFromBusinessPremises.routes.PeopleLivingAtBusinessPremisesController.onPageLoad(taxYear, businessId, NormalMode)

  override def cyaPage(taxYear: TaxYear, businessId: BusinessId): Call =
    routes.WorkplaceRunningCostsCYAController.onPageLoad(taxYear, businessId)

  def previousPagesAreAnswered(request: DataRequest[_], businessId: BusinessId): Boolean
}
