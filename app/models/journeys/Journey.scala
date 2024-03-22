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

package models.journeys

import controllers.journeys.expenses
import enumeratum._
import models.Mode
import models.common.{BusinessId, PageName, TaxYear}
import pages.QuestionPage
import pages.abroad.SelfEmploymentAbroadPage
import pages.capitalallowances.annualInvestmentAllowance.{AnnualInvestmentAllowanceAmountPage, AnnualInvestmentAllowancePage}
import pages.capitalallowances.balancingAllowance.{BalancingAllowanceAmountPage, BalancingAllowancePage}
import pages.capitalallowances.electricVehicleChargePoints._
import pages.capitalallowances.specialTaxSites._
import pages.capitalallowances.structuresBuildingsAllowance.{StructuresBuildingsAllowancePage, StructuresBuildingsClaimedPage}
import pages.capitalallowances.tailoring.{ClaimCapitalAllowancesPage, SelectCapitalAllowancesPage}
import pages.capitalallowances.writingDownAllowance._
import pages.capitalallowances.zeroEmissionCars._
import pages.capitalallowances.zeroEmissionGoodsVehicle._
import pages.expenses.advertisingOrMarketing.AdvertisingOrMarketingAmountPage
import pages.expenses.construction.{ConstructionIndustryAmountPage, ConstructionIndustryDisallowableAmountPage}
import pages.expenses.depreciation.DepreciationDisallowableAmountPage
import pages.expenses.entertainment.EntertainmentAmountPage
import pages.expenses.financialCharges.{FinancialChargesAmountPage, FinancialChargesDisallowableAmountPage}
import pages.expenses.goodsToSellOrUse.{DisallowableGoodsToSellOrUseAmountPage, GoodsToSellOrUseAmountPage}
import pages.expenses.interest.{InterestAmountPage, InterestDisallowableAmountPage}
import pages.expenses.irrecoverableDebts.{IrrecoverableDebtsAmountPage, IrrecoverableDebtsDisallowableAmountPage}
import pages.expenses.officeSupplies.{OfficeSuppliesAmountPage, OfficeSuppliesDisallowableAmountPage}
import pages.expenses.otherExpenses.{OtherExpensesAmountPage, OtherExpensesDisallowableAmountPage}
import pages.expenses.professionalFees.{ProfessionalFeesAmountPage, ProfessionalFeesDisallowableAmountPage}
import pages.expenses.repairsandmaintenance.{RepairsAndMaintenanceAmountPage, RepairsAndMaintenanceDisallowableAmountPage}
import pages.expenses.staffCosts.{StaffCostsAmountPage, StaffCostsDisallowableAmountPage}
import pages.expenses.tailoring.ExpensesCategoriesPage
import pages.expenses.workplaceRunningCosts.workingFromBusinessPremises._
import pages.expenses.workplaceRunningCosts.workingFromHome._
import pages.income._
import play.api.mvc.PathBindable

sealed abstract class Journey(override val entryName: String) extends EnumEntry {
  override def toString: String = entryName

  /** Used to recognize if there are any answers for that journey. Only leave it Nil if there are no answers to store */
  val pageKeys: List[PageName]

  def startUrl(taxYear: TaxYear, businessId: BusinessId, mode: Mode): String = {
    val _ = (taxYear, businessId, mode) // // TODO Remove default impl when all pages are fixed
    ""
  }

  def startPage: QuestionPage[_] = ??? // TODO Remove default impl when all pages are fixed
}

object Journey extends Enum[Journey] with utils.PlayJsonEnum[Journey] {
  val values: IndexedSeq[Journey] = findValues

  implicit def pathBindable(implicit strBinder: PathBindable[String]): PathBindable[Journey] = new PathBindable[Journey] {

    override def bind(key: String, value: String): Either[String, Journey] =
      strBinder.bind(key, value).flatMap { stringValue =>
        Journey.withNameOption(stringValue) match {
          case Some(journeyName) => Right(journeyName)
          case None              => Left(s"$stringValue Invalid journey name")
        }
      }

    override def unbind(key: String, journeyName: Journey): String =
      strBinder.unbind(key, journeyName.entryName)
  }

  case object TradeDetails extends Journey("trade-details") {
    override val pageKeys: List[PageName] = Nil
  }
  case object Abroad extends Journey("self-employment-abroad") {
    override val pageKeys: List[PageName] = List(
      SelfEmploymentAbroadPage.pageName
    )
  }
  case object Income extends Journey("income") {
    override val pageKeys: List[PageName] = List(
      AnyOtherIncomePage.pageName,
      HowMuchTradingAllowancePage.pageName,
      IncomeCYAPage.pageName,
      IncomeNotCountedAsTurnoverPage.pageName,
      NonTurnoverIncomeAmountPage.pageName,
      OtherIncomeAmountPage.pageName,
      TradingAllowanceAmountPage.pageName,
      TradingAllowancePage.pageName,
      TurnoverIncomeAmountPage.pageName,
      TurnoverNotTaxablePage.pageName
    )
  }
  case object ExpensesTotal extends Journey("expenses-total") {
    override val pageKeys: List[PageName] = Nil
  }
  case object ExpensesTailoring extends Journey("expenses-categories") {
    override val pageKeys: List[PageName] = List(
      ExpensesCategoriesPage.pageName
    )
    override def startUrl(taxYear: TaxYear, businessId: BusinessId, mode: Mode): String =
      expenses.tailoring.routes.ExpensesCategoriesController.onPageLoad(taxYear, businessId, mode).url

    override def startPage: QuestionPage[_] = ExpensesCategoriesPage
  }
  case object ExpensesGoodsToSellOrUse extends Journey("expenses-goods-to-sell-or-use") {
    override val pageKeys: List[PageName] = List(GoodsToSellOrUseAmountPage.pageName, DisallowableGoodsToSellOrUseAmountPage.pageName)
  }
  case object ExpensesAdvertisingOrMarketing extends Journey("expenses-advertising-marketing") {
    override val pageKeys: List[PageName] = List(AdvertisingOrMarketingAmountPage.pageName)
  }
  case object ExpensesOfficeSupplies extends Journey("expenses-office-supplies") {
    override val pageKeys: List[PageName] = List(OfficeSuppliesAmountPage.pageName, OfficeSuppliesDisallowableAmountPage.pageName)
  }
  case object ExpensesOtherExpenses extends Journey("expenses-other-expenses") {
    override val pageKeys: List[PageName] = List(OtherExpensesAmountPage.pageName, OtherExpensesDisallowableAmountPage.pageName)
  }
  case object ExpensesFinancialCharges extends Journey("expenses-financial-charges") {
    override val pageKeys: List[PageName] = List(FinancialChargesAmountPage.pageName, FinancialChargesDisallowableAmountPage.pageName)
  }
  case object ExpensesEntertainment extends Journey("expenses-entertainment") {
    override val pageKeys: List[PageName] = List(EntertainmentAmountPage.pageName)
  }
  case object ExpensesStaffCosts extends Journey("expenses-staff-costs") {
    override val pageKeys: List[PageName] = List(StaffCostsAmountPage.pageName, StaffCostsDisallowableAmountPage.pageName)
  }
  case object ExpensesConstruction extends Journey("expenses-construction") {
    override val pageKeys: List[PageName] = List(ConstructionIndustryAmountPage.pageName, ConstructionIndustryDisallowableAmountPage.pageName)
  }
  case object ExpensesProfessionalFees extends Journey("expenses-professional-fees") {
    override val pageKeys: List[PageName] = List(ProfessionalFeesAmountPage.pageName, ProfessionalFeesDisallowableAmountPage.pageName)
  }
  case object ExpensesInterest extends Journey("expenses-interest") {
    override val pageKeys: List[PageName] = List(InterestAmountPage.pageName, InterestDisallowableAmountPage.pageName)
  }
  case object ExpensesDepreciation extends Journey("expenses-depreciation") {
    override val pageKeys: List[PageName] = List(DepreciationDisallowableAmountPage.pageName)
  }

  case object ExpensesRepairsAndMaintenance extends Journey("expenses-repairs-and-maintenance") {
    override val pageKeys: List[PageName] = List(RepairsAndMaintenanceAmountPage.pageName, RepairsAndMaintenanceDisallowableAmountPage.pageName)
  }

  case object ExpensesWorkplaceRunningCosts extends Journey("expenses-workplace-running-costs") {
    override val pageKeys: List[PageName] = List(
      BusinessPremisesAmountPage.pageName,
      LiveAtBusinessPremisesPage.pageName,
      LivingAtBusinessPremisesOnePerson.pageName,
      LivingAtBusinessPremisesTwoPeople.pageName,
      LivingAtBusinessPremisesThreePlusPeople.pageName,
      PeopleLivingAtBusinessPremisesPage.pageName,
      WfbpClaimingAmountPage.pageName,
      WfbpFlatRateOrActualCostsPage.pageName,
      MoreThan25HoursPage.pageName,
      WfhClaimingAmountPage.pageName,
      WfhExpensesInfoPage.pageName,
      WfhFlatRateOrActualCostsPage.pageName,
      WorkingFromHomeHours25To50.pageName,
      WorkingFromHomeHours51To100.pageName,
      WorkingFromHomeHours101Plus.pageName,
      WorkingFromHomeHoursPage.pageName
    )
  }

  case object ExpensesIrrecoverableDebts extends Journey("expenses-irrecoverable-debts") {
    override val pageKeys: List[PageName] = List(IrrecoverableDebtsAmountPage.pageName, IrrecoverableDebtsDisallowableAmountPage.pageName)
  }

  case object NationalInsurance extends Journey("national-insurance") {
    override val pageKeys: List[PageName] = Nil
  }

  sealed abstract class CapitalAllowanceBaseJourney(override val entryName: String) extends Journey(entryName) {
    override def toString: String = entryName
  }

  case object CapitalAllowancesTailoring extends CapitalAllowanceBaseJourney("capital-allowances-tailoring") {
    override val pageKeys: List[PageName] = List(ClaimCapitalAllowancesPage.pageName, SelectCapitalAllowancesPage.pageName)
  }

  case object CapitalAllowancesZeroEmissionCars extends CapitalAllowanceBaseJourney("capital-allowances-zero-emission-cars") {
    override val pageKeys: List[PageName] = List(
      ZeroEmissionCarsPage.pageName,
      ZecAllowancePage.pageName,
      ZecTotalCostOfCarPage.pageName,
      ZecOnlyForSelfEmploymentPage.pageName,
      ZecUseOutsideSEPage.pageName,
      ZecUseOutsideSEPercentagePage.pageName,
      ZecHowMuchDoYouWantToClaimPage.pageName,
      ZecClaimAmount.pageName
    )
  }

  case object CapitalAllowancesZeroEmissionGoodsVehicle extends CapitalAllowanceBaseJourney("capital-allowances-zero-emission-goods-vehicle") {
    override val pageKeys: List[PageName] = List(
      ZeroEmissionGoodsVehiclePage.pageName,
      ZegvAllowancePage.pageName,
      ZegvTotalCostOfVehiclePage.pageName,
      ZegvOnlyForSelfEmploymentPage.pageName,
      ZegvUseOutsideSEPage.pageName,
      ZegvUseOutsideSEPercentagePage.pageName,
      ZegvHowMuchDoYouWantToClaimPage.pageName,
      ZegvClaimAmountPage.pageName
    )
  }

  case object CapitalAllowancesElectricVehicleChargePoints extends CapitalAllowanceBaseJourney("capital-allowances-electric-vehicle-charge-points") {
    override val pageKeys: List[PageName] = List(
      EVCPAllowancePage.pageName,
      ChargePointTaxReliefPage.pageName,
      AmountSpentOnEvcpPage.pageName,
      EvcpOnlyForSelfEmploymentPage.pageName,
      EvcpUseOutsideSEPage.pageName,
      EvcpUseOutsideSEPercentagePage.pageName,
      EvcpHowMuchDoYouWantToClaimPage.pageName,
      EvcpClaimAmount.pageName
    )
  }

  case object CapitalAllowancesBalancingAllowance extends CapitalAllowanceBaseJourney("capital-allowances-balancing-allowance") {
    override val pageKeys: List[PageName] = List(
      BalancingAllowancePage.pageName,
      BalancingAllowanceAmountPage.pageName
    )
  }

  case object CapitalAllowancesAnnualInvestmentAllowance extends CapitalAllowanceBaseJourney("capital-allowances-annual-investment-allowance") {
    override val pageKeys: List[PageName] = List(
      AnnualInvestmentAllowancePage.pageName,
      AnnualInvestmentAllowanceAmountPage.pageName
    )
  }

  case object CapitalAllowancesStructuresBuildings extends CapitalAllowanceBaseJourney("capital-allowances-structures-buildings") {
    override val pageKeys: List[PageName] = List(
      StructuresBuildingsAllowancePage.pageName,
      StructuresBuildingsClaimedPage.pageName
    )
  }

  case object CapitalAllowancesWritingDownAllowance extends CapitalAllowanceBaseJourney("capital-allowances-writing-down-allowance") {
    override val pageKeys: List[PageName] = List(
      WritingDownAllowancePage.pageName,
      WdaMainRateClaimAmountPage.pageName,
      WdaMainRatePage.pageName,
      WdaSingleAssetClaimAmountsPage.pageName,
      WdaSingleAssetPage.pageName,
      WdaSpecialRateClaimAmountPage.pageName,
      WdaSpecialRatePage.pageName
    )
  }

  case object CapitalAllowancesSpecialTaxSites extends CapitalAllowanceBaseJourney("capital-allowances-special-tax-sites") {
    override val pageKeys: List[PageName] = List(
      SpecialTaxSitesPage.pageName,
      ContractForBuildingConstructionPage.pageName,
      ContractStartDatePage.pageName,
      ConstructionStartDatePage.pageName
    )
  }

}
