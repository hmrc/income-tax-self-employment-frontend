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

package models.common

import controllers.journeys.expenses
import enumeratum._
import models.NormalMode
import pages.abroad.SelfEmploymentAbroadPage
import pages.adjustments.profitOrLoss._
import pages.capitalallowances.annualInvestmentAllowance.{AnnualInvestmentAllowanceAmountPage, AnnualInvestmentAllowancePage}
import pages.capitalallowances.balancingAllowance.{BalancingAllowanceAmountPage, BalancingAllowancePage}
import pages.capitalallowances.balancingCharge.{BalancingChargeAmountPage, BalancingChargePage}
import pages.capitalallowances.specialTaxSites._
import pages.capitalallowances.structuresBuildingsAllowance._
import pages.capitalallowances.tailoring.{ClaimCapitalAllowancesPage, SelectCapitalAllowancesPage}
import pages.capitalallowances.writingDownAllowance._
import pages.capitalallowances.zeroEmissionCars._
import pages.capitalallowances.zeroEmissionGoodsVehicle._
import pages.expenses.advertisingOrMarketing.{AdvertisingOrMarketingAmountPage, AdvertisingOrMarketingDisallowableAmountPage}
import pages.expenses.construction.{ConstructionIndustryAmountPage, ConstructionIndustryDisallowableAmountPage}
import pages.expenses.depreciation.DepreciationDisallowableAmountPage
import pages.expenses.entertainment.EntertainmentAmountPage
import pages.expenses.financialCharges.{FinancialChargesAmountPage, FinancialChargesDisallowableAmountPage}
import pages.expenses.goodsToSellOrUse.{DisallowableGoodsToSellOrUseAmountPage, GoodsToSellOrUseAmountPage, TaxiMinicabOrRoadHaulagePage}
import pages.expenses.interest.{InterestAmountPage, InterestDisallowableAmountPage}
import pages.expenses.irrecoverableDebts.{IrrecoverableDebtsAmountPage, IrrecoverableDebtsDisallowableAmountPage}
import pages.expenses.officeSupplies.{OfficeSuppliesAmountPage, OfficeSuppliesDisallowableAmountPage}
import pages.expenses.otherExpenses.{OtherExpensesAmountPage, OtherExpensesDisallowableAmountPage}
import pages.expenses.professionalFees.{ProfessionalFeesAmountPage, ProfessionalFeesDisallowableAmountPage}
import pages.expenses.repairsandmaintenance.{RepairsAndMaintenanceAmountPage, RepairsAndMaintenanceDisallowableAmountPage}
import pages.expenses.staffCosts.{StaffCostsAmountPage, StaffCostsDisallowableAmountPage}
import pages.expenses.tailoring.ExpensesCategoriesPage
import pages.expenses.tailoring.individualCategories._
import pages.expenses.tailoring.simplifiedExpenses.TotalExpensesPage
import pages.expenses.travelAndAccommodation.{
  SimplifiedExpensesPage,
  TravelAndAccommodationExpenseTypePage,
  TravelForWorkYourMileagePage,
  TravelForWorkYourVehiclePage,
  VehicleExpensesPage,
  VehicleFlatRateChoicePage,
  VehicleTypePage,
  YourFlatRateForVehicleExpensesPage,
  PublicTransportAndAccommodationExpensesPage
}
import pages.expenses.workplaceRunningCosts.workingFromBusinessPremises._
import pages.expenses.workplaceRunningCosts.workingFromHome._
import pages.income._
import pages.nics._
import pages.{CostsNotCoveredPage, Page, QuestionPage, DisallowableTransportAndAccommodationPage}
import play.api.mvc.PathBindable
import queries.Settable

sealed abstract class Journey(override val entryName: String) extends EnumEntry {
  override def toString: String = entryName

  /** Used to recognize if there are any answers for that journey. Only leave it Nil if there are no answers to store */
  val pageKeys: List[PageName]

  def startUrl(taxYear: TaxYear, businessId: BusinessId): String = {
    val _ = (taxYear, businessId) // // TODO Remove default impl when all pages are fixed
    ""
  }

  def startPage: QuestionPage[_] = ??? // TODO Remove default impl when all pages are fixed
}

//noinspection ScalaStyle
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

  case object BusinessDetailsPrepop extends Journey("business-details-prepop") {
    override val pageKeys: List[PageName] = Nil
  }

  case object IncomePrepop extends Journey("income-prepop") {
    override val pageKeys: List[PageName] = Nil
  }

  case object SelfEmploymentPrepop extends Journey("self-employment-details-prepop") {
    override val pageKeys: List[PageName] = Nil
  }

  case object ExpensesPrepop extends Journey("expenses-prepop") {
    override val pageKeys: List[PageName] = Nil
  }

  case object CapitalAllowancesPrepop extends Journey("capital-allowances-prepop") {
    override val pageKeys: List[PageName] = Nil
  }

  case object AdjustmentsPrepop extends Journey("adjustments-prepop") {
    override val pageKeys: List[PageName] = Nil
  }

  case object ExpensesTailoring extends Journey("expenses-categories") {
    override val pageKeys: List[PageName] = List(
      ExpensesCategoriesPage.pageName
    )

    override def startUrl(taxYear: TaxYear, businessId: BusinessId): String =
      expenses.tailoring.routes.ExpensesCategoriesController.onPageLoad(taxYear, businessId, NormalMode).url

    override def startPage: QuestionPage[_] = ExpensesCategoriesPage
  }

  case object ExpensesGoodsToSellOrUse extends Journey("expenses-goods-to-sell-or-use") {
    override val pageKeys: List[PageName] = List(GoodsToSellOrUseAmountPage.pageName, DisallowableGoodsToSellOrUseAmountPage.pageName)
  }

  case object ExpensesTravelForWork extends Journey("travel-expenses") {
    override val pageKeys: List[PageName] =
      List(
        TravelAndAccommodationExpenseTypePage.pageName,
        TravelForWorkYourVehiclePage.pageName,
        VehicleTypePage.pageName,
        SimplifiedExpensesPage.pageName,
        VehicleFlatRateChoicePage.pageName,
        TravelForWorkYourMileagePage.pageName,
        YourFlatRateForVehicleExpensesPage.pageName,
        CostsNotCoveredPage.pageName,
        VehicleExpensesPage.pageName,
        PublicTransportAndAccommodationExpensesPage.pageName,
        DisallowableTransportAndAccommodationPage.pageName
      )
  }

  case object ExpensesVehicleDetails extends Journey("vehicle-details") {
    override val pageKeys: List[PageName] = List(TravelForWorkYourVehiclePage.pageName)
  }

  case object ExpensesAdvertisingOrMarketing extends Journey("expenses-advertising-marketing") {
    override val pageKeys: List[PageName] = List(AdvertisingOrMarketingAmountPage.pageName, AdvertisingOrMarketingDisallowableAmountPage.pageName)
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

  sealed abstract class CapitalAllowanceBaseJourney(override val entryName: String) extends Journey(entryName) {
    override def toString: String = entryName

    val answerPages: List[Settable[_]]
    lazy val pageKeys: List[PageName] = answerPages.asInstanceOf[List[Page]].map(_.pageName)
  }

  case object CapitalAllowancesTailoring extends CapitalAllowanceBaseJourney("capital-allowances-tailoring") {
    override val answerPages: List[Settable[_]] = List(ClaimCapitalAllowancesPage, SelectCapitalAllowancesPage)

  }

  case object CapitalAllowancesZeroEmissionCars extends CapitalAllowanceBaseJourney("capital-allowances-zero-emission-cars") {
    override val answerPages: List[Settable[_]] = List(
      ZeroEmissionCarsPage,
      ZecAllowancePage,
      ZecTotalCostOfCarPage,
      ZecOnlyForSelfEmploymentPage,
      ZecUseOutsideSEPage,
      ZecUseOutsideSEPercentagePage,
      ZecHowMuchDoYouWantToClaimPage,
      ZecClaimAmount
    )
  }

  case object CapitalAllowancesZeroEmissionGoodsVehicle extends CapitalAllowanceBaseJourney("capital-allowances-zero-emission-goods-vehicle") {
    override val answerPages: List[Settable[_]] = List(
      ZeroEmissionGoodsVehiclePage,
      ZegvAllowancePage,
      ZegvTotalCostOfVehiclePage,
      ZegvOnlyForSelfEmploymentPage,
      ZegvUseOutsideSEPage,
      ZegvUseOutsideSEPercentagePage,
      ZegvHowMuchDoYouWantToClaimPage,
      ZegvClaimAmountPage
    )
  }

  case object CapitalAllowancesBalancingAllowance extends CapitalAllowanceBaseJourney("capital-allowances-balancing-allowance") {
    override val answerPages: List[Settable[_]] = List(
      BalancingAllowancePage,
      BalancingAllowanceAmountPage
    )
  }

  case object CapitalAllowancesBalancingCharge extends CapitalAllowanceBaseJourney("capital-allowances-balancing-charge") {
    override val answerPages: List[Settable[_]] = List(
      BalancingChargePage,
      BalancingChargeAmountPage
    )
  }

  case object CapitalAllowancesAnnualInvestmentAllowance extends CapitalAllowanceBaseJourney("capital-allowances-annual-investment-allowance") {
    override val answerPages: List[Settable[_]] = List(
      AnnualInvestmentAllowancePage,
      AnnualInvestmentAllowanceAmountPage
    )
  }

  case object CapitalAllowancesStructuresBuildings extends CapitalAllowanceBaseJourney("capital-allowances-structures-buildings") {
    override val answerPages: List[Settable[_]] = List(
      NewStructuresBuildingsList,
      StructuresBuildingsAllowancePage,
      StructuresBuildingsClaimedPage,
      StructuresBuildingsEligibleClaimPage,
      StructuresBuildingsLocationPage,
      StructuresBuildingsNewClaimAmountPage,
      StructuresBuildingsNewStructuresPage,
      StructuresBuildingsPreviousClaimUsePage,
      StructuresBuildingsPreviousClaimedAmountPage,
      StructuresBuildingsQualifyingExpenditurePage,
      StructuresBuildingsQualifyingUseDatePage,
      StructuresBuildingsRemovePage
    )
  }

  case object CapitalAllowancesWritingDownAllowance extends CapitalAllowanceBaseJourney("capital-allowances-writing-down-allowance") {
    override val answerPages: List[Settable[_]] = List(
      WritingDownAllowancePage,
      WdaMainRateClaimAmountPage,
      WdaMainRatePage,
      WdaSingleAssetClaimAmountsPage,
      WdaSingleAssetPage,
      WdaSpecialRateClaimAmountPage,
      WdaSpecialRatePage
    )
  }

  case object CapitalAllowancesSpecialTaxSites extends CapitalAllowanceBaseJourney("capital-allowances-special-tax-sites") {
    override val answerPages: List[Settable[_]] = List(
      SpecialTaxSitesPage,
      NewSpecialTaxSitesList,
      DoYouHaveAContinuingClaimPage,
      ContinueClaimingAllowanceForExistingSitePage,
      ExistingSiteClaimingAmountPage
    )
  }

  sealed abstract class NationalInsuranceBaseJourney(override val entryName: String) extends Journey(entryName) {
    override def toString: String = entryName
  }

  case object NationalInsuranceContributions extends NationalInsuranceBaseJourney("national-insurance-contributions") {
    override val pageKeys: List[PageName] =
      List(Class2NICsPage, Class4NICsPage, Class4ExemptionReasonPage, Class4DivingExemptPage, Class4NonDivingExemptPage).map(_.pageName)
  }

  sealed abstract class AdjustmentsBaseJourney(override val entryName: String) extends Journey(entryName) {
    override def toString: String = entryName
  }

  case object ProfitOrLoss extends AdjustmentsBaseJourney("profit-or-loss") {
    override val pageKeys: List[PageName] = List(
      GoodsAndServicesForYourOwnUsePage,
      GoodsAndServicesAmountPage,
      ClaimLossReliefPage,
      PreviousUnusedLossesPage,
      UnusedLossAmountPage,
      WhichYearIsLossReportedPage
    ).map(_.pageName)
  }

  val allExpensesJourneys: List[Journey] = List(
    ExpensesTailoring,
    ExpensesGoodsToSellOrUse,
    ExpensesTravelForWork,
    ExpensesAdvertisingOrMarketing,
    ExpensesOfficeSupplies,
    ExpensesOtherExpenses,
    ExpensesFinancialCharges,
    ExpensesEntertainment,
    ExpensesStaffCosts,
    ExpensesConstruction,
    ExpensesProfessionalFees,
    ExpensesInterest,
    ExpensesDepreciation,
    ExpensesRepairsAndMaintenance,
    ExpensesWorkplaceRunningCosts,
    ExpensesIrrecoverableDebts,
    ExpensesDepreciation
  )

  val expensesTailoringList: List[Settable[_]] = List(
    TotalExpensesPage,
    AdvertisingOrMarketingAmountPage,
    AdvertisingOrMarketingDisallowableAmountPage,
    AdvertisingOrMarketingPage,
    BusinessPremisesAmountPage,
    BusinessPremisesDisallowableAmountPage,
    ConstructionIndustryAmountPage,
    ConstructionIndustryDisallowableAmountPage,
    DepreciationDisallowableAmountPage,
    DepreciationPage,
    DisallowableGoodsToSellOrUseAmountPage,
    DisallowableInterestPage,
    DisallowableIrrecoverableDebtsPage,
    DisallowableOtherFinancialChargesPage,
    DisallowableProfessionalFeesPage,
    DisallowableStaffCostsPage,
    DisallowableSubcontractorCostsPage,
    EntertainmentAmountPage,
    EntertainmentCostsPage,
    FinancialChargesAmountPage,
    FinancialChargesDisallowableAmountPage,
    FinancialExpensesPage,
    GoodsToSellOrUseAmountPage,
    GoodsToSellOrUsePage,
    InterestAmountPage,
    InterestDisallowableAmountPage,
    IrrecoverableDebtsAmountPage,
    IrrecoverableDebtsDisallowableAmountPage,
    LiveAtBusinessPremisesPage,
    LivingAtBusinessPremisesOnePerson,
    LivingAtBusinessPremisesThreePlusPeople,
    LivingAtBusinessPremisesTwoPeople,
    MoreThan25HoursPage,
    OfficeSuppliesAmountPage,
    OfficeSuppliesDisallowableAmountPage,
    OfficeSuppliesPage,
    OtherExpensesAmountPage,
    OtherExpensesDisallowableAmountPage,
    OtherExpensesPage,
    ProfessionalFeesAmountPage,
    ProfessionalFeesDisallowableAmountPage,
    ProfessionalServiceExpensesPage,
    RepairsAndMaintenanceAmountPage,
    RepairsAndMaintenanceDisallowableAmountPage,
    RepairsAndMaintenancePage,
    StaffCostsAmountPage,
    StaffCostsDisallowableAmountPage,
    TaxiMinicabOrRoadHaulagePage,
    TravelForWorkPage,
    WfbpClaimingAmountPage,
    WfbpFlatRateOrActualCostsPage,
    WfhClaimingAmountPage,
    WfhFlatRateOrActualCostsPage,
    WorkFromBusinessPremisesPage,
    WorkFromHomePage,
    WorkingFromHomeHours25To50,
    WorkingFromHomeHours51To100,
    WorkingFromHomeHours101Plus
  )

  val allExpensesJourneyPages: List[Settable[_]] = expensesTailoringList.appended(ExpensesCategoriesPage)

  val allCapitalAllowanceJourneys: List[Journey] = List(
    CapitalAllowancesTailoring,
    CapitalAllowancesZeroEmissionCars,
    CapitalAllowancesZeroEmissionGoodsVehicle,
    CapitalAllowancesBalancingAllowance,
    CapitalAllowancesAnnualInvestmentAllowance,
    CapitalAllowancesStructuresBuildings,
    CapitalAllowancesWritingDownAllowance,
    CapitalAllowancesSpecialTaxSites
  )

  val allCapitalAllowanceJourneyPages: List[Settable[_]] =
    allCapitalAllowanceJourneys.asInstanceOf[List[CapitalAllowanceBaseJourney]].flatMap(_.answerPages)

}
