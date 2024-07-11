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

package pages.expenses.repairsandmaintenance

import controllers.journeys.expenses.repairsandmaintenance.routes
import models.NormalMode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.RepairsAndMaintenance
import pages.OneQuestionPage
import pages.expenses.tailoring.individualCategories.RepairsAndMaintenancePage
import play.api.mvc.Call

case object RepairsAndMaintenanceAmountPage extends OneQuestionPage[BigDecimal] {
  override def toString: String = "repairsAndMaintenanceAmount"

  override def nextPageInNormalMode(userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear): Call =
    if (hasDisallowable(businessId, userAnswers)) routes.RepairsAndMaintenanceDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)
    else routes.RepairsAndMaintenanceCostsCYAController.onPageLoad(taxYear, businessId)

  override def hasAllFurtherAnswers(businessId: BusinessId, userAnswers: UserAnswers): Boolean =
    userAnswers.get(this, businessId).isDefined &&
      (!hasDisallowable(businessId, userAnswers) || RepairsAndMaintenanceDisallowableAmountPage.hasAllFurtherAnswers(businessId, userAnswers))

  private def hasDisallowable(businessId: BusinessId, userAnswers: UserAnswers): Boolean =
    userAnswers.get(RepairsAndMaintenancePage, businessId).contains(RepairsAndMaintenance.YesDisallowable)
}
