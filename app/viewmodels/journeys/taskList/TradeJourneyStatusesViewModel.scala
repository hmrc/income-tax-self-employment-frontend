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

package viewmodels.journeys.taskList

import controllers.journeys.{abroad, income}
import models._
import models.common.JourneyStatus.CannotStartYet
import models.common.{BusinessId, JourneyStatus, TaxYear, TradingName, TypeOfBusiness}
import models.database.UserAnswers
import models.journeys.Journey
import models.journeys.Journey._
import models.requests.TradesJourneyStatuses
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow, Value}
import viewmodels.govuk.summarylist._
import viewmodels.journeys.taskList.CapitalAllowancesTasklist.buildCapitalAllowances
import viewmodels.journeys.taskList.ExpensesTasklist.buildExpensesCategories
import viewmodels.journeys.{SummaryListCYA, determineJourneyStartOrCyaUrl, getJourneyStatus}

case class TradeJourneyStatusesViewModel(tradingName: TradingName, typeOfBusiness: TypeOfBusiness, businessId: BusinessId, statusList: SummaryList)

object TradeJourneyStatusesViewModel {

  def buildSummaryList(tradesJourneyStatuses: TradesJourneyStatuses, taxYear: TaxYear, userAnswers: Option[UserAnswers])(implicit
      messages: Messages): SummaryList = {
    implicit val impTaxYear: TaxYear                       = taxYear
    implicit val businessId: BusinessId                    = tradesJourneyStatuses.businessId
    implicit val impJourneyStatuses: TradesJourneyStatuses = tradesJourneyStatuses
    implicit val impUserAnswers: Option[UserAnswers]       = userAnswers

    val abroadRow = buildRow(Abroad, dependentJourneyIsFinishedForClickableLink = true)

    val isAbroadAnswered = tradesJourneyStatuses.getStatusOrNotStarted(Abroad).isCompleted
    val incomeRow        = buildRow(Income, dependentJourneyIsFinishedForClickableLink = isAbroadAnswered)

    val rows: List[SummaryListRow] =
      List(abroadRow, incomeRow) ++
        buildExpensesCategories ++
        buildCapitalAllowances(tradesJourneyStatuses, taxYear)

    SummaryListCYA.summaryList(rows)
  }

  private def buildRow(journey: Journey, dependentJourneyIsFinishedForClickableLink: Boolean)(implicit
      messages: Messages,
      taxYear: TaxYear,
      businessId: BusinessId,
      journeyStatuses: TradesJourneyStatuses): SummaryListRow = {
    val status: JourneyStatus = getJourneyStatus(journey, dependentJourneyIsFinishedForClickableLink)(journeyStatuses.journeyStatuses)
    val keyString             = messages(s"journeys.$journey")
    val href = journey match {
      case Abroad => getAbroadUrl(status, businessId, taxYear)
      case Income => getIncomeUrl(status, businessId, taxYear)
      case _      => "#"
    }

    buildSummaryRow(href, keyString, status)
  }

  private[viewmodels] def buildSummaryRow(href: String, keyString: String, status: JourneyStatus)(implicit messages: Messages) = {
    val statusString     = messages(s"status.${status.entryName}")
    val optDeadlinkStyle = if (status == CannotStartYet) s" class='govuk-deadlink'" else ""
    SummaryListRowViewModel(
      key = KeyViewModel(
        HtmlContent(s"<span class='app-task-list__task-name govuk-!-font-weight-regular'> <a href=$href$optDeadlinkStyle> $keyString </a> </span>")),
      value = Value(),
      actions = Seq(
        ActionItemViewModel(
          href = href,
          content = HtmlContent(s"<strong class='govuk-tag app-task-list__tag govuk-tag--$status'> $statusString </strong>"))
          .withCssClass("tag-float"))
    ).withCssClass("app-task-list__item no-wrap no-after-content")
  }

  private def getAbroadUrl(journeyStatus: JourneyStatus, businessId: BusinessId, taxYear: TaxYear): String =
    determineJourneyStartOrCyaUrl(
      abroad.routes.SelfEmploymentAbroadController.onPageLoad(taxYear, businessId, NormalMode).url,
      abroad.routes.SelfEmploymentAbroadCYAController.onPageLoad(taxYear, businessId).url
    )(journeyStatus)
  private def getIncomeUrl(journeyStatus: JourneyStatus, businessId: BusinessId, taxYear: TaxYear): String =
    determineJourneyStartOrCyaUrl(
      income.routes.IncomeNotCountedAsTurnoverController.onPageLoad(taxYear, businessId, NormalMode).url,
      income.routes.IncomeCYAController.onPageLoad(taxYear, businessId).url
    )(journeyStatus)

}
