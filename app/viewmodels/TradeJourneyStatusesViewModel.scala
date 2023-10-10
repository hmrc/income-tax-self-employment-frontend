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

package viewmodels

import controllers.journeys.abroad.routes.SelfEmploymentAbroadController
import models._
import models.requests.TradesJourneyStatuses
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, SummaryList, SummaryListRow, Value}
import viewmodels.govuk.summarylist._

case class TradeJourneyStatusesViewModel(tradingName: String,
                                         businessId: String,
                                         statusList: SummaryList)

object TradeJourneyStatusesViewModel {

  private val completedStatus = "completed"
  private val inProgressStatus = "inProgress"
  private val notStartedStatus = "notStarted"
  private val cannotStartYetStatus = "cannotStartYet"

  def buildSummaryList(business: TradesJourneyStatuses, taxYear: Int)(implicit messages: Messages): SummaryList = {

    val getStatus = (journey: Journey) => {
      val optJourney = business.journeyStatuses.filter(_.journey.equals(journey))
      if (optJourney.isEmpty) None else optJourney.head.completedState
    }

    val (abroadCompletionStatus, incomeCompletionStatus) =
      (getStatus(Abroad), getStatus(Income))

    val (abroadUrlString, incomeUrlString) =
      (SelfEmploymentAbroadController.onPageLoad(taxYear, business.businessId, if (abroadCompletionStatus.isEmpty) NormalMode else CheckMode).url,
        if (abroadCompletionStatus.getOrElse(false)) "#" else "#" //TODO replace first # with income journey url when created
      )

    val (abroadStatusString, incomeStatusString) =
      (
        if (abroadCompletionStatus.isEmpty) notStartedStatus
        else if (abroadCompletionStatus.get) completedStatus
        else inProgressStatus,

        if (!abroadCompletionStatus.getOrElse(false)) cannotStartYetStatus
        else if (incomeCompletionStatus.isEmpty) notStartedStatus
        else if (incomeCompletionStatus.get) completedStatus
        else inProgressStatus,
      )

    SummaryList(
      rows = Seq(
        buildRow("selfEmploymentAbroad", abroadUrlString, abroadStatusString),
        buildRow("income", incomeUrlString, incomeStatusString)
      ),
      classes = "govuk-!-margin-bottom-7")
  }

  private def buildRow(rowKey: String, href: String, status: String)(implicit messages: Messages): SummaryListRow = {

    val keyString = messages(s"common.$rowKey")
    val statusString = messages(s"status.$status")
    val optDeadlinkStyle = if (status.equals(cannotStartYetStatus)) s" class='govuk-deadlink'" else ""

    SummaryListRowViewModel(
      key = KeyViewModel(HtmlContent(
        s"<span class='app-task-list__task-name govuk-!-font-weight-regular'> <a href=$href$optDeadlinkStyle> $keyString </a> </span>")),
      value = Value(),
      actions = Seq(ActionItem(
        href = href,
        content = HtmlContent(s"<strong class='govuk-tag app-task-list__tag govuk-tag--$status'> $statusString </strong>")))
    ).withCssClass("app-task-list__item")
  }

}
