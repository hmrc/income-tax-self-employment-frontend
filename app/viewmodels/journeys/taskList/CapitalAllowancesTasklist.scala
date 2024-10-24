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

import cats.implicits.catsSyntaxOptionId
import controllers.journeys.capitalallowances
import models.NormalMode
import models.common.AccountingType.Cash
import models.common.Journey._
import models.common.JourneyStatus.Completed
import models.common.{BusinessId, JourneyStatus, TaxYear}
import models.database.UserAnswers
import models.journeys.capitalallowances.tailoring.CapitalAllowances
import models.journeys.income.TradingAllowance
import models.requests.TradesJourneyStatuses
import pages.capitalallowances.tailoring.SelectCapitalAllowancesPage
import pages.income.TradingAllowancePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.journeys.{conditionPassedForViewableLink, _}
import viewmodels.journeys.taskList.TradeJourneyStatusesViewModel.buildSummaryRow

object CapitalAllowancesTasklist {

  def buildCapitalAllowances(tradesJourneyStatuses: TradesJourneyStatuses, taxYear: TaxYear)(implicit
      messages: Messages,
      businessId: BusinessId,
      userAnswers: Option[UserAnswers]): List[SummaryListRow] = {

    val journeyStatuses = tradesJourneyStatuses.journeyStatuses

    val incomeIsCompleted = tradesJourneyStatuses.getStatusOrNotStarted(Income) == Completed
    val tailoringStatus   = getJourneyStatus(CapitalAllowancesTailoring, incomeIsCompleted)(journeyStatuses)
    val tailoringHref     = getUrl(CapitalAllowancesTailoring, tailoringStatus, businessId, taxYear)
    val isCashHeading: String = userAnswers.map(_.getAccountingType(businessId)) match {
      case Some(Cash) => s".$Cash"
      case _          => ""
    }

    val expensesSelected         = conditionPassedForViewableLink(TradingAllowancePage, List(TradingAllowance.DeclareExpenses))
    val tradingAllowanceSelected = conditionPassedForViewableLink(TradingAllowancePage, List(TradingAllowance.UseTradingAllowance))

    val tailoringRow = returnRowIfConditionPassed(
      buildSummaryRow(tailoringHref, messages(s"journeys.$CapitalAllowancesTailoring$isCashHeading"), tailoringStatus),
      expensesSelected
    )

    val capAllowancesTailoringCompleted = tailoringStatus.isCompleted

    val zeroEmissionCarsRow = getAllowanceRow(
      taxYear,
      CapitalAllowancesZeroEmissionCars,
      CapitalAllowances.ZeroEmissionCar,
      tradesJourneyStatuses,
      capAllowancesTailoringCompleted,
      expensesSelected
    )

    val zeroEmissionGoodsVehicleRow = getAllowanceRow(
      taxYear,
      CapitalAllowancesZeroEmissionGoodsVehicle,
      CapitalAllowances.ZeroEmissionGoodsVehicle,
      tradesJourneyStatuses,
      capAllowancesTailoringCompleted,
      expensesSelected
    )

    val specialTaxSitesRow = getAllowanceRow(
      taxYear,
      CapitalAllowancesSpecialTaxSites,
      CapitalAllowances.SpecialTaxSitesStructuresAndBuildings,
      tradesJourneyStatuses,
      capAllowancesTailoringCompleted,
      expensesSelected
    )

    val structuresBuildingsRow = getAllowanceRow(
      taxYear,
      CapitalAllowancesStructuresBuildings,
      CapitalAllowances.StructuresAndBuildings,
      tradesJourneyStatuses,
      capAllowancesTailoringCompleted,
      expensesSelected
    )

    val balancingAllowanceRow = getAllowanceRow(
      taxYear,
      CapitalAllowancesBalancingAllowance,
      CapitalAllowances.Balancing,
      tradesJourneyStatuses,
      capAllowancesTailoringCompleted,
      expensesSelected
    )

    val balancingChargeStatus = getJourneyStatus(CapitalAllowancesBalancingCharge)(journeyStatuses)
    val balancingChargeHref =
      getUrl(CapitalAllowancesBalancingCharge, balancingChargeStatus, businessId, taxYear)
    val balancingChargeIsTailored =
      conditionPassedForViewableLink(SelectCapitalAllowancesPage, CapitalAllowances.BalancingCharge) && capAllowancesTailoringCompleted
    val balancingChargeRow = returnRowIfConditionPassed(
      buildSummaryRow(balancingChargeHref, messages(s"journeys.$CapitalAllowancesBalancingCharge"), balancingChargeStatus),
      (balancingChargeIsTailored && expensesSelected) || tradingAllowanceSelected
    )

    val annualInvestmentAllowanceRow = getAllowanceRow(
      taxYear,
      CapitalAllowancesAnnualInvestmentAllowance,
      CapitalAllowances.AnnualInvestment,
      tradesJourneyStatuses,
      capAllowancesTailoringCompleted,
      expensesSelected
    )

    val writingDownRow = getAllowanceRow(
      taxYear,
      CapitalAllowancesWritingDownAllowance,
      CapitalAllowances.WritingDown,
      tradesJourneyStatuses,
      capAllowancesTailoringCompleted,
      expensesSelected)

    List(
      tailoringRow,
      zeroEmissionCarsRow,
      zeroEmissionGoodsVehicleRow,
      structuresBuildingsRow,
      specialTaxSitesRow,
      annualInvestmentAllowanceRow,
      writingDownRow,
      balancingAllowanceRow,
      balancingChargeRow
    ).flatten
  }

  private def getAllowanceRow(taxYear: TaxYear,
                              journey: CapitalAllowanceBaseJourney,
                              allowanceType: CapitalAllowances,
                              tradesJourneyStatuses: TradesJourneyStatuses,
                              capAllowancesTailoringCompleted: Boolean,
                              expensesSelected: Boolean)(implicit
      businessId: BusinessId,
      messages: Messages,
      userAnswers: Option[UserAnswers]
  ): Option[SummaryListRow] = {
    val status     = getJourneyStatus(journey)(tradesJourneyStatuses.journeyStatuses)
    val href       = getUrl(journey, status, businessId, taxYear)
    val isTailored = conditionPassedForViewableLink(SelectCapitalAllowancesPage, allowanceType) && capAllowancesTailoringCompleted

    returnRowIfConditionPassed(buildSummaryRow(href, messages(s"journeys.$journey"), status), isTailored && expensesSelected)
  }

  private def getUrl(journey: CapitalAllowanceBaseJourney, journeyStatus: JourneyStatus, businessId: BusinessId, taxYear: TaxYear): String =
    journey match {
      case CapitalAllowancesTailoring =>
        determineJourneyStartOrCyaUrl(
          capitalallowances.tailoring.routes.ClaimCapitalAllowancesController.onPageLoad(taxYear, businessId, NormalMode).url,
          capitalallowances.tailoring.routes.CapitalAllowanceCYAController.onPageLoad(taxYear, businessId).url
        )(journeyStatus)
      case CapitalAllowancesZeroEmissionCars =>
        determineJourneyStartOrCyaUrl(
          capitalallowances.zeroEmissionCars.routes.ZeroEmissionCarsController.onPageLoad(taxYear, businessId, NormalMode).url,
          capitalallowances.zeroEmissionCars.routes.ZeroEmissionCarsCYAController.onPageLoad(taxYear, businessId).url
        )(journeyStatus)
      case CapitalAllowancesZeroEmissionGoodsVehicle =>
        determineJourneyStartOrCyaUrl(
          capitalallowances.zeroEmissionGoodsVehicle.routes.ZeroEmissionGoodsVehicleController.onPageLoad(taxYear, businessId, NormalMode).url,
          capitalallowances.zeroEmissionGoodsVehicle.routes.ZeroEmissionGoodsVehicleCYAController.onPageLoad(taxYear, businessId).url
        )(journeyStatus)
      case CapitalAllowancesStructuresBuildings =>
        determineJourneyStartOrCyaUrl(
          capitalallowances.structuresBuildingsAllowance.routes.StructuresBuildingsAllowanceController
            .onPageLoad(taxYear, businessId, NormalMode)
            .url,
          capitalallowances.structuresBuildingsAllowance.routes.StructuresBuildingsCYAController.onPageLoad(taxYear, businessId).url
        )(journeyStatus)
      case CapitalAllowancesSpecialTaxSites =>
        determineJourneyStartOrCyaUrl(
          capitalallowances.specialTaxSites.routes.SpecialTaxSitesController.onPageLoad(taxYear, businessId, NormalMode).url,
          capitalallowances.specialTaxSites.routes.SpecialTaxSitesCYAController.onPageLoad(taxYear, businessId).url
        )(journeyStatus)
      case CapitalAllowancesBalancingAllowance =>
        determineJourneyStartOrCyaUrl(
          capitalallowances.balancingAllowance.routes.BalancingAllowanceController.onPageLoad(taxYear, businessId, NormalMode).url,
          capitalallowances.balancingAllowance.routes.BalancingAllowanceCYAController.onPageLoad(taxYear, businessId).url
        )(journeyStatus)
      case CapitalAllowancesBalancingCharge =>
        determineJourneyStartOrCyaUrl(
          capitalallowances.balancingCharge.routes.BalancingChargeController.onPageLoad(taxYear, businessId, NormalMode).url,
          capitalallowances.balancingCharge.routes.BalancingChargeCYAController.onPageLoad(taxYear, businessId).url
        )(journeyStatus)
      case CapitalAllowancesAnnualInvestmentAllowance =>
        determineJourneyStartOrCyaUrl(
          capitalallowances.annualInvestmentAllowance.routes.AnnualInvestmentAllowanceController.onPageLoad(taxYear, businessId, NormalMode).url,
          capitalallowances.annualInvestmentAllowance.routes.AnnualInvestmentAllowanceCYAController.onPageLoad(taxYear, businessId).url
        )(journeyStatus)
      case CapitalAllowancesWritingDownAllowance =>
        determineJourneyStartOrCyaUrl(
          capitalallowances.writingDownAllowance.routes.WritingDownAllowanceController.onPageLoad(taxYear, businessId, NormalMode).url,
          capitalallowances.writingDownAllowance.routes.WritingDownAllowanceCYAController.onPageLoad(taxYear, businessId).url
        )(journeyStatus)
      case _ => ???
    }
}
