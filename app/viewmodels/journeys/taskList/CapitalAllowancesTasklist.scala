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
import models.requests.TradesJourneyStatuses
import pages.capitalallowances.tailoring.SelectCapitalAllowancesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.journeys._
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
    val tailoringRow = buildSummaryRow(tailoringHref, messages(s"journeys.$CapitalAllowancesTailoring$isCashHeading"), tailoringStatus).some

    val capAllowancesTailoringCompleted = tailoringStatus.isCompleted

    val zeroEmissionCarsStatus = getJourneyStatus(CapitalAllowancesZeroEmissionCars)(journeyStatuses)
    val zeroEmissionCarsHref   = getUrl(CapitalAllowancesZeroEmissionCars, zeroEmissionCarsStatus, businessId, taxYear)
    val zecIsTailored =
      conditionPassedForViewableLink(SelectCapitalAllowancesPage, CapitalAllowances.ZeroEmissionCar) && capAllowancesTailoringCompleted
    val zeroEmissionCarsRow = returnRowIfConditionPassed(
      buildSummaryRow(zeroEmissionCarsHref, messages(s"journeys.$CapitalAllowancesZeroEmissionCars"), zeroEmissionCarsStatus),
      zecIsTailored
    )

    val zeroEmissionGoodsVehicleStatus = getJourneyStatus(CapitalAllowancesZeroEmissionGoodsVehicle)(journeyStatuses)
    val zeroEmissionGoodsVehicleHref =
      getUrl(CapitalAllowancesZeroEmissionGoodsVehicle, zeroEmissionGoodsVehicleStatus, businessId, taxYear)
    val zegvIsTailored =
      conditionPassedForViewableLink(SelectCapitalAllowancesPage, CapitalAllowances.ZeroEmissionGoodsVehicle) && capAllowancesTailoringCompleted
    val zeroEmissionGoodsVehicleRow = returnRowIfConditionPassed(
      buildSummaryRow(zeroEmissionGoodsVehicleHref, messages(s"journeys.$CapitalAllowancesZeroEmissionGoodsVehicle"), zeroEmissionGoodsVehicleStatus),
      zegvIsTailored
    )

    val specialTaxSitesRow = getAllowanceRow(
      taxYear,
      CapitalAllowancesSpecialTaxSites,
      CapitalAllowances.SpecialTaxSitesStructuresAndBuildings,
      tradesJourneyStatuses,
      capAllowancesTailoringCompleted
    )

    val structuresBuildingsStatus = getJourneyStatus(CapitalAllowancesStructuresBuildings)(journeyStatuses)
    val structuresBuildingsHref   = getUrl(CapitalAllowancesStructuresBuildings, structuresBuildingsStatus, businessId, taxYear)
    val structuresBuildingsIsTailored =
      conditionPassedForViewableLink(SelectCapitalAllowancesPage, CapitalAllowances.StructuresAndBuildings) && capAllowancesTailoringCompleted
    val structuresBuildingsRow = returnRowIfConditionPassed(
      buildSummaryRow(structuresBuildingsHref, messages(s"journeys.$CapitalAllowancesStructuresBuildings"), structuresBuildingsStatus),
      structuresBuildingsIsTailored
    )

    val balancingAllowanceStatus = getJourneyStatus(CapitalAllowancesBalancingAllowance)(journeyStatuses)
    val balancingAllowanceHref =
      getUrl(CapitalAllowancesBalancingAllowance, balancingAllowanceStatus, businessId, taxYear)
    val balancingAllowanceIsTailored =
      conditionPassedForViewableLink(SelectCapitalAllowancesPage, CapitalAllowances.Balancing) && capAllowancesTailoringCompleted
    val balancingAllowanceRow = returnRowIfConditionPassed(
      buildSummaryRow(balancingAllowanceHref, messages(s"journeys.$CapitalAllowancesBalancingAllowance"), balancingAllowanceStatus),
      balancingAllowanceIsTailored
    )

    val balancingChargeStatus = getJourneyStatus(CapitalAllowancesBalancingCharge)(journeyStatuses)
    val balancingChargeHref =
      getUrl(CapitalAllowancesBalancingCharge, balancingChargeStatus, businessId, taxYear)
    val balancingChargeIsTailored =
      conditionPassedForViewableLink(SelectCapitalAllowancesPage, CapitalAllowances.BalancingCharge) && capAllowancesTailoringCompleted
    val balancingChargeRow = returnRowIfConditionPassed(
      buildSummaryRow(balancingChargeHref, messages(s"journeys.$CapitalAllowancesBalancingCharge"), balancingChargeStatus),
      balancingChargeIsTailored
    )

    val annualInvestmentAllowanceStatus = getJourneyStatus(CapitalAllowancesAnnualInvestmentAllowance)(journeyStatuses)
    val annualInvestmentAllowanceHref =
      getUrl(CapitalAllowancesAnnualInvestmentAllowance, annualInvestmentAllowanceStatus, businessId, taxYear)
    val annualInvestmentAllowanceIsTailored =
      conditionPassedForViewableLink(SelectCapitalAllowancesPage, CapitalAllowances.AnnualInvestment) && capAllowancesTailoringCompleted
    val annualInvestmentAllowanceRow = returnRowIfConditionPassed(
      buildSummaryRow(
        annualInvestmentAllowanceHref,
        messages(s"journeys.$CapitalAllowancesAnnualInvestmentAllowance"),
        annualInvestmentAllowanceStatus),
      annualInvestmentAllowanceIsTailored
    )

    val writingDownRow = getAllowanceRow(
      taxYear,
      CapitalAllowancesWritingDownAllowance,
      CapitalAllowances.WritingDown,
      tradesJourneyStatuses,
      capAllowancesTailoringCompleted)

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
                              capAllowancesTailoringCompleted: Boolean)(implicit
      businessId: BusinessId,
      messages: Messages,
      userAnswers: Option[UserAnswers]
  ): Option[SummaryListRow] = {
    val status     = getJourneyStatus(journey)(tradesJourneyStatuses.journeyStatuses)
    val href       = getUrl(journey, status, businessId, taxYear)
    val isTailored = conditionPassedForViewableLink(SelectCapitalAllowancesPage, allowanceType) && capAllowancesTailoringCompleted

    returnRowIfConditionPassed(buildSummaryRow(href, messages(s"journeys.$journey"), status), isTailored)
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
      case CapitalAllowancesBalancingCharge => "#"
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
