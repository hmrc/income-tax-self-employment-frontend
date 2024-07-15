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

import controllers.journeys.nics
import models.NormalMode
import models.common.{JourneyStatus, TaxYear}
import models.journeys.Journey.{NationalInsuranceContributions, ProfitOrLoss}
import models.journeys.JourneyNameAndStatus
import models.requests.TradesJourneyStatuses
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.journeys.taskList.TradeJourneyStatusesViewModel.buildSummaryRow
import viewmodels.journeys.{SummaryListCYA, getJourneyStatus}

object NationalInsuranceContributionsViewModel {

  def isAdjustmentsAnswered(tradeStatuses: List[TradesJourneyStatuses]): Boolean =
    tradeStatuses.nonEmpty && tradeStatuses.forall(s => JourneyStatus.getJourneyStatus(ProfitOrLoss, s.journeyStatuses).isCompleted)

  def buildSummaryList(nationalInsuranceStatuses: Option[JourneyNameAndStatus], tradeStatuses: List[TradesJourneyStatuses], taxYear: TaxYear)(implicit
      messages: Messages): SummaryList = {

    val nicRow =
      buildRow(nationalInsuranceStatuses, dependentJourneyIsFinishedForClickableLink = isAdjustmentsAnswered(tradeStatuses), taxYear)

    SummaryListCYA.summaryList(List(nicRow))
  }

  private def buildRow(nationalInsuranceStatuses: Option[JourneyNameAndStatus],
                       dependentJourneyIsFinishedForClickableLink: Boolean,
                       taxYear: TaxYear)(implicit messages: Messages): SummaryListRow = {
    val status: JourneyStatus = getJourneyStatus(NationalInsuranceContributions, dependentJourneyIsFinishedForClickableLink)(
      nationalInsuranceStatuses.fold(List.empty[JourneyNameAndStatus])(List(_)))

    val keyString = messages(s"journeys.$NationalInsuranceContributions")
    val href      = nics.routes.Class2NICsController.onPageLoad(taxYear, NormalMode).url

    buildSummaryRow(href, keyString, status)
  }
}
