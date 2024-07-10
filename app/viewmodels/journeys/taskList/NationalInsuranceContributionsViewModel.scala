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
import models.journeys.Journey.{Adjustments, NationalInsuranceContributions}
import models.journeys.{Journey, JourneyNameAndStatus}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.journeys.taskList.TradeJourneyStatusesViewModel.buildSummaryRow
import viewmodels.journeys.{SummaryListCYA, getJourneyStatus}

object NationalInsuranceContributionsViewModel {

  def buildSummaryList(nationalInsuranceStatuses: List[JourneyNameAndStatus], taxYear: TaxYear)(implicit messages: Messages): SummaryList = {

    val isAdjustmentsAnswered = JourneyStatus.getJourneyStatus(Adjustments, nationalInsuranceStatuses).isCompleted
    val nicRow = buildRow(
      NationalInsuranceContributions,
      nationalInsuranceStatuses,
      dependentJourneyIsFinishedForClickableLink = isAdjustmentsAnswered)(messages, taxYear)

    val rows: List[SummaryListRow] =
      List(nicRow)

    SummaryListCYA.summaryList(rows)
  }

  private def buildRow(journey: Journey,
                       nationalInsuranceStatuses: List[JourneyNameAndStatus],
                       dependentJourneyIsFinishedForClickableLink: Boolean)(implicit messages: Messages, taxYear: TaxYear): SummaryListRow = {
    val status: JourneyStatus = getJourneyStatus(journey, dependentJourneyIsFinishedForClickableLink)(nationalInsuranceStatuses)
    val keyString             = messages(s"journeys.$journey")
    val href = journey match {
      case NationalInsuranceContributions => nics.routes.Class2NICsController.onPageLoad(taxYear, NormalMode).url
      case _                              => "#"
    }

    buildSummaryRow(href, keyString, status)
  }

}
