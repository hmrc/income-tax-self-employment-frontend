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

package viewmodels.journeys.taskList

import controllers.journeys.capitalallowances
import models.NormalMode
import models.common.JourneyStatus.Completed
import models.common.{BusinessId, JourneyStatus, TaxYear}
import models.journeys.Journey.{Abroad, CapitalAllowancesTailoring}
import models.requests.TradesJourneyStatuses
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.journeys.taskList.TradeJourneyStatusesViewModel.buildSummaryRow
import viewmodels.journeys.{determineJourneyStartOrCyaUrl, checkIfCannotStartYet, returnRowIfConditionPassed}

object CapitalAllowancesTasklist {

  def buildCapitalAllowances(tradesJourneyStatuses: TradesJourneyStatuses, taxYear: TaxYear, businessId: BusinessId)(implicit
      messages: Messages): List[SummaryListRow] = {
    val abroadIsCompleted = tradesJourneyStatuses.getStatusOrNotStarted(Abroad) == Completed
    val tailoringStatus   = checkIfCannotStartYet(CapitalAllowancesTailoring)(tradesJourneyStatuses)
    val tailoringHref     = getCapitalAllowanceUrl(tailoringStatus, businessId, taxYear)
    val tailoringRow = returnRowIfConditionPassed(
      buildSummaryRow(tailoringHref, messages(s"journeys.$CapitalAllowancesTailoring"), tailoringStatus),
      conditionIsPassed = abroadIsCompleted)

    List(tailoringRow).flatten
  }

  private def getCapitalAllowanceUrl(journeyStatus: JourneyStatus, businessId: BusinessId, taxYear: TaxYear): String =
    determineJourneyStartOrCyaUrl(
      capitalallowances.tailoring.routes.ClaimCapitalAllowancesController.onPageLoad(taxYear, businessId, NormalMode).url,
      capitalallowances.tailoring.routes.CapitalAllowanceCYAController.onPageLoad(taxYear, businessId).url
    )(journeyStatus)
}
