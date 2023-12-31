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
import models.journeys.expenses.individualCategories._
import models.journeys.income.TradingAllowance
import models.requests.TradesJourneyStatuses
import pages.OneQuestionPage
import pages.expenses.tailoring.individualCategories._
import pages.income.TradingAllowancePage
import play.api.i18n.Messages
import play.api.libs.json.Reads
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow, Value}
import viewmodels.govuk.summarylist._
import viewmodels.journeys.SummaryListCYA

case class TradeJourneyStatusesViewModel(tradingName: String, businessId: BusinessId, statusList: SummaryList)

// TODO This is over complex class and needs to be simplified
object TradeJourneyStatusesViewModel {

  // noinspection ScalaStyle
  def buildSummaryList(tradesJourneyStatuses: TradesJourneyStatuses, taxYear: TaxYear, userAnswers: Option[UserAnswers])(implicit
      messages: Messages): SummaryList = {

    implicit val impTaxYear: TaxYear                       = taxYear
    implicit val businessId: BusinessId                    = tradesJourneyStatuses.businessId
    implicit val impJourneyStatuses: TradesJourneyStatuses = tradesJourneyStatuses
    implicit val impUserAnswers: Option[UserAnswers]       = userAnswers

    val expensesTailoringIsAnswered: Boolean = dependentJourneyIsInProgressOrCompleted(ExpensesTailoring)

    SummaryListCYA.summaryList(
      List(
        buildRow(Abroad),
        buildRow(
          Income,
          dependentJourneyIsFinishedForClickableLink = dependentJourneyIsInProgressOrCompleted(Abroad, checkOnlyCompleted = true)
        ),
        buildRow(
          ExpensesTailoring,
          conditionPassedForViewableLink(TradingAllowancePage, Seq(TradingAllowance.DeclareExpenses)),
          dependentJourneyIsInProgressOrCompleted(Income, checkOnlyCompleted = true)
        ),
        buildRow(
          ExpensesOfficeSupplies,
          expensesTailoringIsAnswered && conditionPassedForViewableLink(OfficeSuppliesPage, OfficeSupplies.values.filterNot(_ == OfficeSupplies.No))
        ),
        buildRow(
          ExpensesGoodsToSellOrUse,
          expensesTailoringIsAnswered && conditionPassedForViewableLink(
            GoodsToSellOrUsePage,
            GoodsToSellOrUse.values.filterNot(_ == GoodsToSellOrUse.No))
        ),
        buildRow(
          ExpensesRepairsAndMaintenance,
          expensesTailoringIsAnswered && conditionPassedForViewableLink(
            RepairsAndMaintenancePage,
            RepairsAndMaintenance.values.filterNot(_ == RepairsAndMaintenance.No))
        ),
        buildRow(
          ExpensesAdvertisingOrMarketing,
          expensesTailoringIsAnswered && conditionPassedForViewableLink(
            AdvertisingOrMarketingPage,
            AdvertisingOrMarketing.values.filterNot(_ == AdvertisingOrMarketing.No))
        ),
        buildRow(
          ExpensesEntertainment,
          expensesTailoringIsAnswered && conditionPassedForViewableLink(
            EntertainmentCostsPage,
            EntertainmentCosts.values.filterNot(_ == EntertainmentCosts.No))
        ),
        buildRow(
          ExpensesStaffCosts,
          expensesTailoringIsAnswered && conditionPassedForViewableLink(ProfessionalServiceExpensesPage, ProfessionalServiceExpenses.Staff)),
        buildRow(
          ExpensesConstruction,
          expensesTailoringIsAnswered && conditionPassedForViewableLink(
            ProfessionalServiceExpensesPage,
            ProfessionalServiceExpenses.Construction
          )
        ),
        buildRow(
          ExpensesProfessionalFees,
          expensesTailoringIsAnswered && conditionPassedForViewableLink(
            ProfessionalServiceExpensesPage,
            ProfessionalServiceExpenses.ProfessionalFees
          )
        ),
        buildRow(
          ExpensesInterest,
          expensesTailoringIsAnswered && conditionPassedForViewableLink(
            FinancialExpensesPage,
            FinancialExpenses.Interest
          )
        ),
        buildRow(
          ExpensesFinancialCharges,
          expensesTailoringIsAnswered && conditionPassedForViewableLink(
            FinancialExpensesPage,
            FinancialExpenses.OtherFinancialCharges
          )
        ),
        buildRow(
          ExpensesDepreciation,
          expensesTailoringIsAnswered && conditionPassedForViewableLink(DepreciationPage, Depreciation.values.filterNot(_ == Depreciation.No))
        )
      ).flatten
    )
  }

  private def buildRow(journey: Journey, conditionPassedForViewableLink: Boolean = true, dependentJourneyIsFinishedForClickableLink: Boolean = true)(
      implicit
      messages: Messages,
      taxYear: TaxYear,
      businessId: BusinessId,
      journeyStatuses: TradesJourneyStatuses): Option[SummaryListRow] =
    if (conditionPassedForViewableLink) {
      val status: JourneyStatus = getJourneyStatus(journey, dependentJourneyIsFinishedForClickableLink)
      val keyString             = messages(s"journeys.$journey")
      val statusString          = messages(s"status.${status.entryName}")
      val optDeadlinkStyle      = if (status == CannotStartYet) s" class='govuk-deadlink'" else ""
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

  private def dependentJourneyIsInProgressOrCompleted(dependentJourney: Journey, checkOnlyCompleted: Boolean = false)(implicit
      journeyStatuses: TradesJourneyStatuses): Boolean =
    getJourneyStatus(dependentJourney) match {
      case Completed | InProgress if !checkOnlyCompleted => true
      case Completed                                     => true
      case _                                             => false
    }

  private def getJourneyStatus(journey: Journey, dependentJourneyIsFinishedForClickableLink: Boolean = true)(implicit
      journeyStatuses: TradesJourneyStatuses): JourneyStatus =
    JourneyStatus.getJourneyStatus(journey, journeyStatuses) match {
      case NotStarted if !dependentJourneyIsFinishedForClickableLink => CannotStartYet
      case NotStarted                                                => NotStarted
      case status: JourneyStatus                                     => status
    }

  private def conditionPassedForViewableLink[A](page: OneQuestionPage[A], acceptableAnswers: Seq[A])(implicit
      businessId: BusinessId,
      userAnswers: Option[UserAnswers],
      readsA: Reads[A]): Boolean =
    getAnswer(page).fold(false)(acceptableAnswers.contains(_))

  private def conditionPassedForViewableLink[A](page: OneQuestionPage[Set[A]], requiredAnswer: A)(implicit
      businessId: BusinessId,
      userAnswers: Option[UserAnswers],
      readsA: Reads[A]): Boolean =
    getAnswer(page).fold(false)(_.contains(requiredAnswer))

  private def getAnswer[A](page: OneQuestionPage[A])(implicit businessId: BusinessId, userAnswers: Option[UserAnswers], reads: Reads[A]): Option[A] =
    userAnswers.flatMap(_.get(page, Some(businessId)))

  // noinspection ScalaStyle
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
      case ExpensesRepairsAndMaintenance =>
        determineUrl(
          expenses.repairsandmaintenance.routes.RepairsAndMaintenanceAmountController.onPageLoad(taxYear, businessId, NormalMode).url,
          expenses.repairsandmaintenance.routes.RepairsAndMaintenanceCostsCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesAdvertisingOrMarketing =>
        determineUrl(
          expenses.advertisingOrMarketing.routes.AdvertisingAmountController.onPageLoad(taxYear, businessId, NormalMode).url,
          expenses.advertisingOrMarketing.routes.AdvertisingCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesEntertainment =>
        determineUrl(
          expenses.entertainment.routes.EntertainmentAmountController.onPageLoad(taxYear, businessId, NormalMode).url,
          expenses.entertainment.routes.EntertainmentCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesStaffCosts =>
        determineUrl(
          expenses.staffCosts.routes.StaffCostsAmountController
            .onPageLoad(taxYear, businessId, NormalMode)
            .url,
          expenses.staffCosts.routes.StaffCostsCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesConstruction =>
        determineUrl(
          expenses.construction.routes.ConstructionIndustryAmountController
            .onPageLoad(taxYear, businessId, NormalMode)
            .url,
          expenses.construction.routes.ConstructionIndustryCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesProfessionalFees =>
        determineUrl(
          expenses.professionalFees.routes.ProfessionalFeesAmountController
            .onPageLoad(taxYear, businessId, NormalMode)
            .url,
          expenses.professionalFees.routes.ProfessionalFeesCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesInterest =>
        determineUrl(
          expenses.interest.routes.InterestAmountController
            .onPageLoad(taxYear, businessId, NormalMode)
            .url,
          expenses.interest.routes.InterestCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesFinancialCharges =>
        determineUrl(
          expenses.financialCharges.routes.FinancialChargesAmountController
            .onPageLoad(taxYear, businessId, NormalMode)
            .url,
          expenses.financialCharges.routes.FinancialChargesCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesDepreciation =>
        determineUrl(
          expenses.depreciation.routes.DepreciationDisallowableAmountController
            .onPageLoad(taxYear, businessId, NormalMode)
            .url,
          expenses.depreciation.routes.DepreciationCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesTotal | NationalInsurance | TradeDetails | ExpensesAdvertisingOrMarketing | ExpensesOtherExpenses =>
        ??? // TODO Other Journeys not yet implemented
    }
  }

  private def determineUrl(startUrl: String, cyaUrl: String)(implicit status: JourneyStatus): String =
    status match {
      case CannotStartYet               => "#"
      case Completed | InProgress       => cyaUrl
      case NotStarted | CheckOurRecords => startUrl
    }

}
