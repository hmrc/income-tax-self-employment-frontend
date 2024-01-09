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

package viewmodels.checkAnswers.expenses.repairsandmaintenance

import controllers.journeys.expenses.repairsandmaintenance.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.RepairsAndMaintenance
import models.journeys.expenses.individualCategories.RepairsAndMaintenance._
import models.requests.DataRequest
import pages.expenses.repairsandmaintenance.{RepairsAndMaintenanceAmountPage, RepairsAndMaintenanceDisallowableAmountPage}
import pages.expenses.tailoring.individualCategories.RepairsAndMaintenancePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.MoneyUtils.formatMoney
import viewmodels.checkAnswers.buildRowBigDecimal

object RepairsAndMaintenanceDisallowableAmountSummary {

  def row(request: DataRequest[_], taxYear: TaxYear, businessId: BusinessId)(implicit messages: Messages): Option[SummaryListRow] =
    request.userAnswers
      .get(RepairsAndMaintenancePage, Some(businessId))
      .filter(areAnyDisallowable)
      .flatMap(_ => createSummaryListRow(request.userAnswers, taxYear, businessId, request.userType))

  private def createSummaryListRow(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): Option[SummaryListRow] =
    for {
      disallowableAmount <- answers.get(RepairsAndMaintenanceDisallowableAmountPage, Some(businessId))
      allowableAmount    <- answers.get(RepairsAndMaintenanceAmountPage, Some(businessId))
    } yield buildRowBigDecimal(
      disallowableAmount,
      routes.RepairsAndMaintenanceDisallowableAmountController.onPageLoad(taxYear, businessId, CheckMode),
      messages(s"repairsAndMaintenanceDisallowableAmount.title.$userType", formatMoney(allowableAmount)),
      "repairsAndMaintenanceDisallowableAmount.change.hidden"
    )

  private def areAnyDisallowable(repairsAndMaintenance: RepairsAndMaintenance): Boolean =
    repairsAndMaintenance match {
      case YesDisallowable   => true
      case YesAllowable | No => false
    }

}
