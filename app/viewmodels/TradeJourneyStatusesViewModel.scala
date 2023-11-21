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

import controllers.journeys.abroad.routes.{SelfEmploymentAbroadCYAController, SelfEmploymentAbroadController}
import controllers.journeys.expenses.tailoring.routes.OfficeSuppliesController
import controllers.journeys.income.routes.{IncomeCYAController, IncomeNotCountedAsTurnoverController}
import models._
import models.journeys.Journey
import models.journeys.Journey.{Abroad, ExpensesTailoring, Income}
import models.requests.TradesJourneyStatuses
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow, Value}
import viewmodels.govuk.summarylist._

case class TradeJourneyStatusesViewModel(tradingName: String, businessId: String, statusList: SummaryList)

object TradeJourneyStatusesViewModel {

  private val completedStatus      = "completed"
  private val inProgressStatus     = "inProgress"
  private val notStartedStatus     = "notStarted"
  private val cannotStartYetStatus = "cannotStartYet"

  def buildSummaryList(business: TradesJourneyStatuses, taxYear: Int)(implicit messages: Messages): SummaryList = {

    val (abroadCompletionStatus, incomeCompletionStatus, expensesCompletionStatus) = (
      getStatus(business, Abroad),
      getStatus(business, Income, Some(Abroad)),
      getStatus(business, ExpensesTailoring, Some(Abroad))
    )

    val (abroadUrl, incomeUrl, expensesUrl) = (
      sortUrl(
        abroadCompletionStatus,
        SelfEmploymentAbroadController.onPageLoad(taxYear, business.businessId, NormalMode).url,
        SelfEmploymentAbroadCYAController.onPageLoad(taxYear, business.businessId).url
      ),
      sortUrl(
        incomeCompletionStatus,
        IncomeNotCountedAsTurnoverController.onPageLoad(taxYear, business.businessId, NormalMode).url,
        IncomeCYAController.onPageLoad(taxYear, business.businessId).url
      ),
      sortUrl(
        expensesCompletionStatus,
        OfficeSuppliesController.onPageLoad(taxYear, business.businessId, NormalMode).url, // TODO expenses categories page when built
        OfficeSuppliesController.onPageLoad(taxYear, business.businessId, NormalMode).url // TODO expenses CYA page when built
      )
    )

    SummaryList(
      rows = Seq(
        buildRow("selfEmploymentAbroad", abroadUrl, abroadCompletionStatus),
        buildRow("income", incomeUrl, incomeCompletionStatus),
        buildRow("expensesCategories", expensesUrl, expensesCompletionStatus)
      ),
      classes = "govuk-!-margin-bottom-7"
    )
  }

  private def buildRow(rowKey: String, href: String, status: String)(implicit messages: Messages): SummaryListRow = {

    val keyString        = messages(s"common.$rowKey")
    val statusString     = messages(s"status.$status")
    val optDeadlinkStyle = if (status.equals(cannotStartYetStatus)) s" class='govuk-deadlink'" else ""

    SummaryListRowViewModel(
      key = KeyViewModel(
        HtmlContent(s"<span class='app-task-list__task-name govuk-!-font-weight-regular'> <a href=$href$optDeadlinkStyle> $keyString </a> </span>")),
      value = Value(),
      actions = Seq(ActionItemViewModel(
        href = href,
        content = HtmlContent(s"<strong class='govuk-tag app-task-list__tag govuk-tag--$status'> $statusString </strong>")).withCssClass("tag-float"))
    ).withCssClass("app-task-list__item no-wrap no-after-content")
  }

  private def getStatus(business: TradesJourneyStatuses, journey: Journey, conditionalCompletedJourney: Option[Journey] = None): String =
    getCompletedState(business, journey) match {
      case Some(true)  => completedStatus
      case Some(false) => inProgressStatus
//      case _ =>
//        conditionalCompletedJourney
//          .filterNot(j => getCompletedState(business, j).contains(true))
//          .map(_ => cannotStartYetStatus)
//          .getOrElse(notStartedStatus)
      case _ =>
        conditionalCompletedJourney match { // if CCJ = Some(false) => CSYS else NSS
          case Some(journey) if !getCompletedState(business, journey).getOrElse(true) => cannotStartYetStatus
          case _ => notStartedStatus
        }
    }

  private def getCompletedState(business: TradesJourneyStatuses, journey: Journey): Option[Boolean] =
    business.journeyStatuses.find(_.journey == journey).flatMap(_.completedState)

  private def sortUrl(status: String, startUrl: String, cyaUrl: String): String =
    status match {
      case `cannotStartYetStatus` => "#"
      case `completedStatus`      => cyaUrl
      case _                      => startUrl
    }

}
