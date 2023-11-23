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

import controllers.journeys.{abroad, expenses, income}
import models._
import models.common.{BusinessId, TaxYear}
import models.journeys.Journey
import models.journeys.Journey.{
  Abroad,
  ExpensesEntertainment,
  ExpensesGoodsToSellOrUse,
  ExpensesOfficeSupplies,
  ExpensesRepairsAndMaintenance,
  ExpensesStaffCosts,
  ExpensesTailoring,
  ExpensesTotal,
  Income,
  NationalInsurance,
  TradeDetails
}
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

  def buildSummaryList(tradesJourneyStatuses: TradesJourneyStatuses, taxYear: Int)(implicit messages: Messages): SummaryList = {

    implicit val impTaxYear: TaxYear                       = TaxYear(taxYear)
    implicit val businessId: BusinessId                    = BusinessId(tradesJourneyStatuses.businessId)
    implicit val impJourneyStatuses: TradesJourneyStatuses = tradesJourneyStatuses

    SummaryList(
      rows = Seq(
        buildRow(Abroad),
        buildRow(Income, Some(Abroad)),
        buildRow(ExpensesTailoring, Some(Abroad))
      ),
      classes = "govuk-!-margin-bottom-7"
    )
  }

  private def buildRow(journey: Journey, conditionalCompletedJourney: Option[Journey] = None)(implicit
      messages: Messages,
      taxYear: TaxYear,
      businessId: BusinessId,
      journeyStatuses: TradesJourneyStatuses): SummaryListRow = {

    val status           = getJourneyStatus(journey, conditionalCompletedJourney)
    val keyString        = messages(s"journeys.$journey")
    val statusString     = messages(s"status.$status")
    val optDeadlinkStyle = if (status.equals(cannotStartYetStatus)) s" class='govuk-deadlink'" else ""
    val href             = getUrl(journey, status, businessId, taxYear)

    SummaryListRowViewModel(
      key = KeyViewModel(
        HtmlContent(s"<span class='app-task-list__task-name govuk-!-font-weight-regular'> <a href=$href$optDeadlinkStyle> $keyString </a> </span>")),
      value = Value(),
      actions = Seq(ActionItemViewModel(
        href = href,
        content = HtmlContent(s"<strong class='govuk-tag app-task-list__tag govuk-tag--$status'> $statusString </strong>")).withCssClass("tag-float"))
    ).withCssClass("app-task-list__item no-wrap no-after-content")
  }

  private def getJourneyStatus(journey: Journey, conditionalCompletedJourney: Option[Journey])(implicit
      journeyStatuses: TradesJourneyStatuses): String =
    getCompletedState(journeyStatuses, journey) match {
      case Some(true)  => completedStatus
      case Some(false) => inProgressStatus
      case _ =>
        conditionalCompletedJourney match {
          case Some(journey) if !getCompletedState(journeyStatuses, journey).contains(true) => cannotStartYetStatus
          case _                                                                            => notStartedStatus
        }
    }

  private def getCompletedState(journeyStatuses: TradesJourneyStatuses, journey: Journey): Option[Boolean] =
    journeyStatuses.journeyStatuses.find(_.journey == journey).flatMap(_.completedState)

  private def getUrl(journey: Journey, journeyStatus: String, businessId: BusinessId, taxYear: TaxYear): String = {
    implicit val status: String = journeyStatus
    journey match {
      case Abroad =>
        determineUrl(
          abroad.routes.SelfEmploymentAbroadController.onPageLoad(taxYear.value, businessId.value, NormalMode).url,
          abroad.routes.SelfEmploymentAbroadCYAController.onPageLoad(taxYear.value, businessId.value).url
        )
      case Income =>
        determineUrl(
          income.routes.IncomeNotCountedAsTurnoverController.onPageLoad(taxYear.value, businessId.value, NormalMode).url,
          income.routes.IncomeCYAController.onPageLoad(taxYear.value, businessId.value).url
        )
      case ExpensesTailoring =>
        determineUrl(
          expenses.tailoring.routes.OfficeSuppliesController
            .onPageLoad(taxYear.value, businessId.value, NormalMode)
            .url, // TODO expenses categories page when built
          expenses.tailoring.routes.OfficeSuppliesController
            .onPageLoad(taxYear.value, businessId.value, NormalMode)
            .url // TODO expenses CYA page when built
        )
      case ExpensesEntertainment | ExpensesGoodsToSellOrUse | ExpensesOfficeSupplies | ExpensesRepairsAndMaintenance | ExpensesTotal |
          NationalInsurance | TradeDetails | ExpensesStaffCosts =>
        ??? // TODO Other Journeys not yet implemented

    }
  }

  private def determineUrl(startUrl: String, cyaUrl: String)(implicit status: String): String =
    status match {
      case `cannotStartYetStatus` => "#"
      case `completedStatus`      => cyaUrl
      case _                      => startUrl
    }

}
