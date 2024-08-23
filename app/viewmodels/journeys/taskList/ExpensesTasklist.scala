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

import controllers.journeys.expenses
import models.NormalMode
import models.common.Journey._
import models.common.JourneyStatus.{CheckOurRecords, Completed, InProgress, NotStarted}
import models.common.{BusinessId, Journey, JourneyStatus, TaxYear}
import models.database.UserAnswers
import models.journeys.expenses.individualCategories._
import models.journeys.income.TradingAllowance
import models.requests.TradesJourneyStatuses
import pages.expenses.tailoring.individualCategories._
import pages.income.TradingAllowancePage
import play.api.i18n.Messages
import play.api.libs.json.Reads
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.journeys._
import viewmodels.journeys.taskList.TradeJourneyStatusesViewModel._

object ExpensesTasklist {

  def buildExpensesCategories(implicit
      tradesJourneyStatuses: TradesJourneyStatuses,
      taxYear: TaxYear,
      businessId: BusinessId,
      userAnswers: Option[UserAnswers],
      messages: Messages): List[SummaryListRow] = {

    val isExpensesTailoringIsAnswered = tradesJourneyStatuses.getStatusOrNotStarted(ExpensesTailoring).isCompleted
    val isIncomeAnswered              = isJourneyCompletedOrInProgress(tradesJourneyStatuses, Income)

    val hasDeclaredExpenses  = conditionPassedForViewableLink(TradingAllowancePage, List(TradingAllowance.DeclareExpenses))
    val expensesTailoringRow = buildRow(ExpensesTailoring, hasDeclaredExpenses, isIncomeAnswered)

    val hasOfficeSupplies         = conditionPassedForViewableLink(OfficeSuppliesPage, OfficeSupplies.values.filterNot(_ == OfficeSupplies.No))
    val expensesOfficeSuppliesRow = buildRow(ExpensesOfficeSupplies, isExpensesTailoringIsAnswered && hasOfficeSupplies)

    val hasGoodsToSellOrUse = conditionPassedForViewableLink(GoodsToSellOrUsePage, GoodsToSellOrUse.values.filterNot(_ == GoodsToSellOrUse.No))
    val expensesGoodsToSellOrUseRow = buildRow(ExpensesGoodsToSellOrUse, isExpensesTailoringIsAnswered && hasGoodsToSellOrUse)

    val hasRepairsAndMaintenance =
      conditionPassedForViewableLink(RepairsAndMaintenancePage, RepairsAndMaintenance.values.filterNot(_ == RepairsAndMaintenance.No))
    val expensesRepairsAndMaintenanceRow =
      buildRow(ExpensesRepairsAndMaintenance, isExpensesTailoringIsAnswered && hasRepairsAndMaintenance)

    val hasWorkplaceRunningCosts =
      conditionPassedForViewableLink(WorkFromHomePage, Seq(true)) ||
        conditionPassedForViewableLink(WorkFromBusinessPremisesPage, WorkFromBusinessPremises.values.filterNot(_ == WorkFromBusinessPremises.No))
    val expensesWorkplaceRunningCostsRow = {
      val optWfhMsg = getPageAnswer(WorkFromHomePage).collect { case true => ".wfh" }
      buildWorkplaceRunningCostsRow(ExpensesWorkplaceRunningCosts, isExpensesTailoringIsAnswered && hasWorkplaceRunningCosts, userAnswers, optWfhMsg)
    }

    val hasAdvertisingOrMarketing =
      conditionPassedForViewableLink(AdvertisingOrMarketingPage, AdvertisingOrMarketing.values.filterNot(_ == AdvertisingOrMarketing.No))
    val expensesAdvertisingOrMarketingRow =
      buildRow(ExpensesAdvertisingOrMarketing, isExpensesTailoringIsAnswered && hasAdvertisingOrMarketing)

    val hasEntertainmentCosts    = conditionPassedForViewableLink(EntertainmentCostsPage, Seq(true))
    val expensesEntertainmentRow = buildRow(ExpensesEntertainment, isExpensesTailoringIsAnswered && hasEntertainmentCosts)

    val hasProfessionalServiceExpenses =
      conditionPassedForViewableLink(ProfessionalServiceExpensesPage, ProfessionalServiceExpenses.Staff)
    val expensesStaffCostsRow = buildRow(ExpensesStaffCosts, isExpensesTailoringIsAnswered && hasProfessionalServiceExpenses)

    val hasConstruction         = conditionPassedForViewableLink(ProfessionalServiceExpensesPage, ProfessionalServiceExpenses.Construction)
    val expensesConstructionRow = buildRow(ExpensesConstruction, isExpensesTailoringIsAnswered && hasConstruction)

    val hasProfessionalFees =
      conditionPassedForViewableLink(ProfessionalServiceExpensesPage, ProfessionalServiceExpenses.ProfessionalFees)
    val expensesProfessionalFeesRow = buildRow(ExpensesProfessionalFees, isExpensesTailoringIsAnswered && hasProfessionalFees)

    val hasInterest = conditionPassedForViewableLink(
      FinancialExpensesPage,
      FinancialExpenses.Interest
    )
    val expensesInterestRow = buildRow(ExpensesInterest, isExpensesTailoringIsAnswered && hasInterest)

    val hasOtherFinancialCharges = conditionPassedForViewableLink(
      FinancialExpensesPage,
      FinancialExpenses.OtherFinancialCharges
    )
    val expensesFinancialChargesRow =
      buildRow(ExpensesFinancialCharges, isExpensesTailoringIsAnswered && hasOtherFinancialCharges)

    val hasDepreciation         = conditionPassedForViewableLink(DepreciationPage, Seq(true))
    val expensesDepreciationRow = buildRow(ExpensesDepreciation, isExpensesTailoringIsAnswered && hasDepreciation)

    val hasIrrecoverableDebts = conditionPassedForViewableLink[FinancialExpenses](
      FinancialExpensesPage,
      FinancialExpenses.IrrecoverableDebts
    )
    val irrecoverableDebtsRow =
      buildRow(Journey.ExpensesIrrecoverableDebts, isExpensesTailoringIsAnswered && hasIrrecoverableDebts)

    val hasOtherExpenses         = conditionPassedForViewableLink(OtherExpensesPage, OtherExpenses.values.filterNot(_ == OtherExpenses.No))
    val expensesOtherExpensesRow = buildRow(ExpensesOtherExpenses, isExpensesTailoringIsAnswered && hasOtherExpenses)

    List(
      expensesTailoringRow,
      expensesOfficeSuppliesRow,
      expensesGoodsToSellOrUseRow,
      expensesRepairsAndMaintenanceRow,
      expensesWorkplaceRunningCostsRow,
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

  private def buildRow(journey: Journey, conditionPassedForViewableLink: Boolean, dependentJourneyIsFinishedForClickableLink: Boolean = true)(implicit
      messages: Messages,
      taxYear: TaxYear,
      businessId: BusinessId,
      journeyStatuses: TradesJourneyStatuses): Option[SummaryListRow] = {
    val status: JourneyStatus = getJourneyStatus(journey, dependentJourneyIsFinishedForClickableLink)(journeyStatuses.journeyStatuses)
    val keyString             = messages(s"journeys.$journey")
    val href                  = getExpensesUrl(journey, status, businessId, taxYear)
    val row                   = buildSummaryRow(href, keyString, status)
    returnRowIfConditionPassed(row, conditionPassedForViewableLink)
  }

  private def buildWorkplaceRunningCostsRow(journey: Journey,
                                            conditionPassedForViewableLink: Boolean,
                                            userAnswers: Option[UserAnswers],
                                            optWfhMsg: Option[String])(implicit
      messages: Messages,
      taxYear: TaxYear,
      businessId: BusinessId,
      journeyStatuses: TradesJourneyStatuses): Option[SummaryListRow] = {
    val optWfh    = optWfhMsg.getOrElse("")
    val status    = getJourneyStatus(journey)(journeyStatuses.journeyStatuses)
    val keyString = messages(s"journeys.$journey$optWfh")
    val href      = getWorkplaceRunningCostsUrl(status, userAnswers, taxYear)
    val row       = buildSummaryRow(href, keyString, status)
    returnRowIfConditionPassed(row, conditionPassedForViewableLink)
  }

  private def getExpensesUrl(journey: Journey, journeyStatus: JourneyStatus, businessId: BusinessId, taxYear: TaxYear): String = {
    implicit val status: JourneyStatus = journeyStatus
    journey match {
      case ExpensesTailoring =>
        determineJourneyStartOrCyaUrl(
          ExpensesTailoring.startUrl(taxYear, businessId),
          expenses.tailoring.routes.ExpensesTailoringCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesOfficeSupplies =>
        determineJourneyStartOrCyaUrl(
          expenses.officeSupplies.routes.OfficeSuppliesAmountController.onPageLoad(taxYear, businessId, NormalMode).url,
          expenses.officeSupplies.routes.OfficeSuppliesCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesGoodsToSellOrUse =>
        determineJourneyStartOrCyaUrl(
          expenses.goodsToSellOrUse.routes.TaxiMinicabOrRoadHaulageController.onPageLoad(taxYear, businessId, NormalMode).url,
          expenses.goodsToSellOrUse.routes.GoodsToSellOrUseCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesRepairsAndMaintenance =>
        determineJourneyStartOrCyaUrl(
          expenses.repairsandmaintenance.routes.RepairsAndMaintenanceAmountController.onPageLoad(taxYear, businessId, NormalMode).url,
          expenses.repairsandmaintenance.routes.RepairsAndMaintenanceCostsCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesAdvertisingOrMarketing =>
        determineJourneyStartOrCyaUrl(
          expenses.advertisingOrMarketing.routes.AdvertisingAmountController.onPageLoad(taxYear, businessId, NormalMode).url,
          expenses.advertisingOrMarketing.routes.AdvertisingCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesEntertainment =>
        determineJourneyStartOrCyaUrl(
          expenses.entertainment.routes.EntertainmentAmountController.onPageLoad(taxYear, businessId, NormalMode).url,
          expenses.entertainment.routes.EntertainmentCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesStaffCosts =>
        determineJourneyStartOrCyaUrl(
          expenses.staffCosts.routes.StaffCostsAmountController
            .onPageLoad(taxYear, businessId, NormalMode)
            .url,
          expenses.staffCosts.routes.StaffCostsCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesConstruction =>
        determineJourneyStartOrCyaUrl(
          expenses.construction.routes.ConstructionIndustryAmountController
            .onPageLoad(taxYear, businessId, NormalMode)
            .url,
          expenses.construction.routes.ConstructionIndustryCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesProfessionalFees =>
        determineJourneyStartOrCyaUrl(
          expenses.professionalFees.routes.ProfessionalFeesAmountController
            .onPageLoad(taxYear, businessId, NormalMode)
            .url,
          expenses.professionalFees.routes.ProfessionalFeesCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesInterest =>
        determineJourneyStartOrCyaUrl(
          expenses.interest.routes.InterestAmountController
            .onPageLoad(taxYear, businessId, NormalMode)
            .url,
          expenses.interest.routes.InterestCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesFinancialCharges =>
        determineJourneyStartOrCyaUrl(
          expenses.financialCharges.routes.FinancialChargesAmountController
            .onPageLoad(taxYear, businessId, NormalMode)
            .url,
          expenses.financialCharges.routes.FinancialChargesCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesDepreciation =>
        determineJourneyStartOrCyaUrl(
          expenses.depreciation.routes.DepreciationDisallowableAmountController
            .onPageLoad(taxYear, businessId, NormalMode)
            .url,
          expenses.depreciation.routes.DepreciationCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesIrrecoverableDebts =>
        determineJourneyStartOrCyaUrl(
          expenses.irrecoverableDebts.routes.IrrecoverableDebtsAmountController.onPageLoad(taxYear, businessId, NormalMode).url,
          expenses.irrecoverableDebts.routes.IrrecoverableDebtsCYAController.onPageLoad(taxYear, businessId).url
        )
      case ExpensesOtherExpenses =>
        determineJourneyStartOrCyaUrl(
          expenses.otherExpenses.routes.OtherExpensesAmountController.onPageLoad(taxYear, businessId, NormalMode).url,
          expenses.otherExpenses.routes.OtherExpensesCYAController.onPageLoad(taxYear, businessId).url
        )
      case _ => "#"
    }
  }

  private def getWorkplaceRunningCostsUrl(journeyStatus: JourneyStatus, userAnswers: Option[UserAnswers], taxYear: TaxYear)(implicit
      businessId: BusinessId,
      wfhReads: Reads[Boolean],
      wfbpReads: Reads[WorkFromBusinessPremises]): String = {
    val wfhTailoring: Option[Boolean] = getPageAnswer[Boolean](WorkFromHomePage)(businessId, userAnswers, wfhReads)
    val wfbpTailoring: Option[WorkFromBusinessPremises] =
      getPageAnswer[WorkFromBusinessPremises](WorkFromBusinessPremisesPage)(businessId, userAnswers, wfbpReads)
    val cyaUrl = expenses.workplaceRunningCosts.routes.WorkplaceRunningCostsCYAController.onPageLoad(taxYear, businessId).url
    val wfhUrl = expenses.workplaceRunningCosts.workingFromHome.routes.MoreThan25HoursController.onPageLoad(taxYear, businessId, NormalMode).url
    val wfbpUrl = expenses.workplaceRunningCosts.workingFromBusinessPremises.routes.LiveAtBusinessPremisesController
      .onPageLoad(taxYear, businessId, NormalMode)
      .url
    journeyStatus match {
      case Completed | InProgress                                                                  => cyaUrl
      case NotStarted | CheckOurRecords if wfhTailoring contains true                              => wfhUrl
      case NotStarted | CheckOurRecords if wfbpTailoring exists (_ != WorkFromBusinessPremises.No) => wfbpUrl
      case _                                                                                       => "#"
    }
  }
}
