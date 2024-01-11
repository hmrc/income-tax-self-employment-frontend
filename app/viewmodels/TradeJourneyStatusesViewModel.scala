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
import models.common.{BusinessId, JourneyStatus, TaxYear, TradingName}
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
import cats.implicits._

case class TradeJourneyStatusesViewModel(tradingName: TradingName, businessId: BusinessId, statusList: SummaryList)

// TODO This is over complex class and needs to be simplified
object TradeJourneyStatusesViewModel {

  def buildSummaryList(tradesJourneyStatuses: TradesJourneyStatuses, taxYear: TaxYear, userAnswers: Option[UserAnswers])(implicit
      messages: Messages): SummaryList = {
    implicit val impTaxYear: TaxYear                       = taxYear
    implicit val businessId: BusinessId                    = tradesJourneyStatuses.businessId
    implicit val impJourneyStatuses: TradesJourneyStatuses = tradesJourneyStatuses
    implicit val impUserAnswers: Option[UserAnswers]       = userAnswers

    val abroadRow = buildRowForDependentAnswered(Abroad, conditionPassedForViewableLink = true)

    val isAbroadAnswered = tradesJourneyStatuses.getStatusOrNotStarted(Abroad).isCompleted
    val incomeRow        = buildRow(Income, conditionPassedForViewableLink = true, dependentJourneyIsFinishedForClickableLink = isAbroadAnswered)

    val isIncomeAnswered     = isJourneyCompletedOrInProgress(tradesJourneyStatuses, Income)
    val hasDeclareExpenses   = conditionPassedForViewableLink(TradingAllowancePage, List(TradingAllowance.DeclareExpenses))
    val expensesTailoringRow = buildRow(ExpensesTailoring, hasDeclareExpenses, isIncomeAnswered)
    val expensesCategories   = buildExpensesCategories

    val rows: List[SummaryListRow] =
      List(abroadRow, incomeRow).flatten ++
        List(expensesTailoringRow).flatten ++
        expensesCategories

    SummaryListCYA.summaryList(rows)
  }

  // noinspection ScalaStyle
  private def buildExpensesCategories(implicit
      tradesJourneyStatuses: TradesJourneyStatuses,
      taxYear: TaxYear,
      businessId: BusinessId,
      userAnswers: Option[UserAnswers],
      messages: Messages) = {
    val isExpensesTailoringIsAnswered = tradesJourneyStatuses.getStatusOrNotStarted(ExpensesTailoring).isCompleted

    val hasOfficeSupplies         = conditionPassedForViewableLink(OfficeSuppliesPage, OfficeSupplies.values.filterNot(_ == OfficeSupplies.No))
    val expensesOfficeSuppliesRow = buildRowForDependentAnswered(ExpensesOfficeSupplies, isExpensesTailoringIsAnswered && hasOfficeSupplies)

    val hasGoodsToSellOrUse = conditionPassedForViewableLink(GoodsToSellOrUsePage, GoodsToSellOrUse.values.filterNot(_ == GoodsToSellOrUse.No))
    val expensesGoodsToSellOrUseRow = buildRowForDependentAnswered(ExpensesGoodsToSellOrUse, isExpensesTailoringIsAnswered && hasGoodsToSellOrUse)

    val hasRepairsAndMaintenance =
      conditionPassedForViewableLink(RepairsAndMaintenancePage, RepairsAndMaintenance.values.filterNot(_ == RepairsAndMaintenance.No))
    val expensesRepairsAndMaintenanceRow =
      buildRowForDependentAnswered(ExpensesRepairsAndMaintenance, isExpensesTailoringIsAnswered && hasRepairsAndMaintenance)

    val hasAdvertisingOrMarketing =
      conditionPassedForViewableLink(AdvertisingOrMarketingPage, AdvertisingOrMarketing.values.filterNot(_ == AdvertisingOrMarketing.No))
    val expensesAdvertisingOrMarketingRow =
      buildRowForDependentAnswered(ExpensesAdvertisingOrMarketing, isExpensesTailoringIsAnswered && hasAdvertisingOrMarketing)

    val hasEntertainmentCosts =
      conditionPassedForViewableLink(EntertainmentCostsPage, EntertainmentCosts.values.filterNot(_ == EntertainmentCosts.No))
    val expensesEntertainmentRow = buildRowForDependentAnswered(ExpensesEntertainment, isExpensesTailoringIsAnswered && hasEntertainmentCosts)

    val hasProfessionalServiceExpenses =
      conditionPassedForViewableLink(ProfessionalServiceExpensesPage, ProfessionalServiceExpenses.Staff)
    val expensesStaffCostsRow = buildRowForDependentAnswered(ExpensesStaffCosts, isExpensesTailoringIsAnswered && hasProfessionalServiceExpenses)

    val hasConstruction         = conditionPassedForViewableLink(ProfessionalServiceExpensesPage, ProfessionalServiceExpenses.Construction)
    val expensesConstructionRow = buildRowForDependentAnswered(ExpensesConstruction, isExpensesTailoringIsAnswered && hasConstruction)

    val hasProfessionalFees =
      conditionPassedForViewableLink(ProfessionalServiceExpensesPage, ProfessionalServiceExpenses.ProfessionalFees)
    val expensesProfessionalFeesRow = buildRowForDependentAnswered(ExpensesProfessionalFees, isExpensesTailoringIsAnswered && hasProfessionalFees)

    val hasInterest = conditionPassedForViewableLink(
      FinancialExpensesPage,
      FinancialExpenses.Interest
    )
    val expensesInterestRow = buildRowForDependentAnswered(ExpensesInterest, isExpensesTailoringIsAnswered && hasInterest)

    val hasOtherFinancialCharges = conditionPassedForViewableLink(
      FinancialExpensesPage,
      FinancialExpenses.OtherFinancialCharges
    )
    val expensesFinancialChargesRow =
      buildRowForDependentAnswered(ExpensesFinancialCharges, isExpensesTailoringIsAnswered && hasOtherFinancialCharges)

    val hasDepreciation         = conditionPassedForViewableLink(DepreciationPage, Depreciation.values.filterNot(_ == Depreciation.No))
    val expensesDepreciationRow = buildRowForDependentAnswered(ExpensesDepreciation, isExpensesTailoringIsAnswered && hasDepreciation)

    val hasIrrecoverableDebts = conditionPassedForViewableLink(
      FinancialExpensesPage,
      FinancialExpenses.IrrecoverableDebts
    )
    val irrecoverableDebtsRow =
      buildRowForDependentAnswered(Journey.ExpensesIrrecoverableDebts, isExpensesTailoringIsAnswered && hasIrrecoverableDebts)

    val hasOtherExpenses         = conditionPassedForViewableLink(OtherExpensesPage, OtherExpenses.values.filterNot(_ == OtherExpenses.No))
    val expensesOtherExpensesRow = buildRowForDependentAnswered(ExpensesOtherExpenses, isExpensesTailoringIsAnswered && hasOtherExpenses)

    List(
      expensesOfficeSuppliesRow,
      expensesGoodsToSellOrUseRow,
      expensesRepairsAndMaintenanceRow,
      expensesAdvertisingOrMarketingRow,
      expensesEntertainmentRow,
      expensesStaffCostsRow,
      expensesConstructionRow,
      expensesProfessionalFeesRow,
      expensesInterestRow,
      expensesFinancialChargesRow,
      irrecoverableDebtsRow,
      expensesDepreciationRow,
      expensesOtherExpensesRow
    ).flatten
  }

  def isJourneyCompletedOrInProgress(tradesJourneyStatuses: TradesJourneyStatuses, dependentJourney: Journey): Boolean =
    getJourneyStatus(dependentJourney)(tradesJourneyStatuses) match {
      case Completed | InProgress                        => true
      case CheckOurRecords | CannotStartYet | NotStarted => false
    }

  private def buildRowForDependentAnswered(journey: Journey, conditionPassedForViewableLink: Boolean)(implicit
      messages: Messages,
      taxYear: TaxYear,
      businessId: BusinessId,
      journeyStatuses: TradesJourneyStatuses): Option[SummaryListRow] =
    buildRow(journey, conditionPassedForViewableLink, dependentJourneyIsFinishedForClickableLink = true)

  private def buildRow(journey: Journey, conditionPassedForViewableLink: Boolean, dependentJourneyIsFinishedForClickableLink: Boolean)(implicit
      messages: Messages,
      taxYear: TaxYear,
      businessId: BusinessId,
      journeyStatuses: TradesJourneyStatuses): Option[SummaryListRow] =
    if (conditionPassedForViewableLink) {
      val status: JourneyStatus = getJourneyStatus(journey, dependentJourneyIsFinishedForClickableLink)
      val keyString             = messages(s"journeys.$journey")
      val optDeadlinkStyle      = if (status == CannotStartYet) s" class='govuk-deadlink'" else ""
      val href                  = getUrl(journey, status, businessId, taxYear)

      buildSummaryRow(href, optDeadlinkStyle, keyString, status).some
    } else {
      None
    }

  private[viewmodels] def buildSummaryRow(href: String, optDeadlinkStyle: String, keyString: String, status: JourneyStatus)(implicit
      messages: Messages) = {
    val statusString = messages(s"status.${status.entryName}")
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
      case ExpensesIrrecoverableDebts =>
        determineUrl(
          expenses.irrecoverableDebts.routes.IrrecoverableDebtsAmountController.onPageLoad(taxYear, businessId, NormalMode).url,
          expenses.irrecoverableDebts.routes.IrrecoverableDebtsCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesOtherExpenses =>
        determineUrl(
          expenses.otherExpenses.routes.OtherExpensesAmountController.onPageLoad(taxYear, businessId, NormalMode).url,
          expenses.otherExpenses.routes.OtherExpensesCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesTotal | NationalInsurance | TradeDetails | ExpensesAdvertisingOrMarketing =>
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
