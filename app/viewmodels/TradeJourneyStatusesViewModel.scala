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
import models.common.JourneyStatus._
import models.common.{BusinessId, JourneyStatus, TaxYear}
import models.database.UserAnswers
import models.journeys.Journey
import models.journeys.Journey._
import models.journeys.expenses.individualCategories.{DisallowableStaffCosts, EntertainmentCosts, GoodsToSellOrUse, OfficeSupplies}
import models.requests.TradesJourneyStatuses
import pages.OneQuestionPage
import pages.expenses.tailoring.individualCategories.{DisallowableStaffCostsPage, EntertainmentCostsPage, GoodsToSellOrUsePage, OfficeSuppliesPage}
import play.api.i18n.Messages
import play.api.libs.json.Reads
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow, Value}
import viewmodels.govuk.summarylist._
import viewmodels.journeys.SummaryListCYA

case class TradeJourneyStatusesViewModel(tradingName: String, businessId: BusinessId, statusList: SummaryList)

object TradeJourneyStatusesViewModel {

  def buildSummaryList(tradesJourneyStatuses: TradesJourneyStatuses, taxYear: TaxYear, userAnswers: Option[UserAnswers])(implicit
      messages: Messages): SummaryList = {

    implicit val impTaxYear: TaxYear                       = taxYear
    implicit val businessId: BusinessId                    = BusinessId(tradesJourneyStatuses.businessId)
    implicit val impJourneyStatuses: TradesJourneyStatuses = tradesJourneyStatuses
    implicit val impUserAnswers: Option[UserAnswers]       = userAnswers

    SummaryListCYA.summaryList(
      List(
        buildRow(Abroad),
        buildRow(Income, Some(Abroad)),
        buildRow(ExpensesTailoring, Some(Abroad)),
        buildRow(ExpensesOfficeSupplies, None, pageMeetsCriteria(OfficeSuppliesPage, OfficeSupplies.values.filterNot(_ == OfficeSupplies.No))),
        buildRow(
          ExpensesGoodsToSellOrUse,
          None,
          pageMeetsCriteria(GoodsToSellOrUsePage, GoodsToSellOrUse.values.filterNot(_ == GoodsToSellOrUse.No))),
        buildRow(
          ExpensesEntertainment,
          None,
          pageMeetsCriteria(EntertainmentCostsPage, EntertainmentCosts.values.filterNot(_ == EntertainmentCosts.No))),
        buildRow(
          ExpensesStaffCosts,
          None,
          pageMeetsCriteria(DisallowableStaffCostsPage, DisallowableStaffCosts.values.filterNot(_ == DisallowableStaffCosts.No)))
      ).flatten
    )
  }

  private def buildRow(journey: Journey, conditionalCompletedJourney: Option[Journey] = None, conditionPassed: Boolean = true)(implicit
      messages: Messages,
      taxYear: TaxYear,
      businessId: BusinessId,
      journeyStatuses: TradesJourneyStatuses): Option[SummaryListRow] =
    if (conditionPassed) {
      val status: JourneyStatus = getJourneyStatus(journey, conditionalCompletedJourney)
      val keyString             = messages(s"journeys.$journey")
      val statusString          = messages(s"status.${status.entryName}")
      val optDeadlinkStyle      = if (status.equals(CannotStartYet)) s" class='govuk-deadlink'" else ""
      val href                  = getUrl(journey, status, businessId, taxYear)

      Some(
        SummaryListRowViewModel(
          key = KeyViewModel(HtmlContent(
            s"<span class='app-task-list__task-name govuk-!-font-weight-regular'> <a href=$href$optDeadlinkStyle> $keyString </a> </span>")),
          value = Value(),
          actions = Seq(
            ActionItemViewModel(
              href = href,
              content = HtmlContent(s"<strong class='govuk-tag app-task-list__tag govuk-tag--$status'> $statusString </strong>"))
              .withCssClass("tag-float"))
        ).withCssClass("app-task-list__item no-wrap no-after-content"))
    } else {
      None
    }

  private def getJourneyStatus(journey: Journey, conditionalCompletedJourney: Option[Journey])(implicit
      journeyStatuses: TradesJourneyStatuses): JourneyStatus =
    statusFromCompletedState(getCompletedState(journeyStatuses, journey)) match {
      case NotStarted            => conditionalJourneyIsPassed(conditionalCompletedJourney)
      case status: JourneyStatus => status
    }

  private def getCompletedState(journeyStatuses: TradesJourneyStatuses, journey: Journey): Option[Boolean] =
    journeyStatuses.journeyStatuses.find(_.journey == journey).flatMap(_.completedState)

  private def conditionalJourneyIsPassed(conditionalCompletedJourney: Option[Journey])(implicit
      journeyStatuses: TradesJourneyStatuses): JourneyStatus =
    conditionalCompletedJourney match {
      case Some(journey) if !getCompletedState(journeyStatuses, journey).contains(true) => CannotStartYet
      case _                                                                            => NotStarted
    }

  private def pageMeetsCriteria[A](page: OneQuestionPage[A], criteria: Seq[A])(implicit
      businessId: BusinessId,
      userAnswers: Option[UserAnswers],
      readsA: Reads[A]): Boolean =
    getAnswer(page).fold(false)(criteria.contains(_))

  private def getAnswer[A](page: OneQuestionPage[A])(implicit businessId: BusinessId, userAnswers: Option[UserAnswers], reads: Reads[A]): Option[A] =
    userAnswers.flatMap(_.get(page, Some(businessId)))

  private def getUrl(journey: Journey, journeyStatus: JourneyStatus, businessId: BusinessId, taxYear: TaxYear): String = {
    implicit val status: JourneyStatus = journeyStatus
    journey match {
      case Abroad =>
        determineUrl(
          abroad.routes.SelfEmploymentAbroadController.onPageLoad(taxYear, businessId, NormalMode).url,
          abroad.routes.SelfEmploymentAbroadCYAController.onPageLoad(taxYear, businessId).url
        )
      case Income =>
        determineUrl(
          income.routes.IncomeNotCountedAsTurnoverController.onPageLoad(taxYear, businessId, NormalMode).url,
          income.routes.IncomeCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesTailoring =>
        determineUrl(
          expenses.tailoring.routes.ExpensesCategoriesController.onPageLoad(taxYear, businessId, NormalMode).url,
          expenses.tailoring.routes.ExpensesTailoringCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesOfficeSupplies =>
        determineUrl(
          expenses.officeSupplies.routes.OfficeSuppliesAmountController.onPageLoad(taxYear, businessId, NormalMode).url,
          expenses.officeSupplies.routes.OfficeSuppliesCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesGoodsToSellOrUse =>
        determineUrl(
          expenses.goodsToSellOrUse.routes.GoodsToSellOrUseAmountController.onPageLoad(taxYear, businessId, NormalMode).url,
          expenses.goodsToSellOrUse.routes.GoodsToSellOrUseCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesEntertainment =>
        determineUrl(
          expenses.entertainment.routes.EntertainmentAmountController
            .onPageLoad(taxYear, businessId, NormalMode)
            .url,
          expenses.entertainment.routes.EntertainmentCYAController
            .onPageLoad(taxYear, businessId)
            .url
        )
      case ExpensesStaffCosts =>
        determineUrl(
          expenses.staffCosts.routes.StaffCostsAmountController
            .onPageLoad(taxYear, businessId, NormalMode)
            .url,
          expenses.staffCosts.routes.StaffCostsCYAController
            .onPageLoad(taxYear, businessId)
            .url
        )
      case ExpensesConstruction | ExpensesRepairsAndMaintenance | ExpensesTotal | NationalInsurance | TradeDetails =>
        ??? // TODO Other Journeys not yet implemented
    }
  }

  private def determineUrl(startUrl: String, cyaUrl: String)(implicit status: JourneyStatus): String =
    status match {
      case CannotStartYet         => "#"
      case Completed | InProgress => cyaUrl
      case _                      => startUrl
    }

}
