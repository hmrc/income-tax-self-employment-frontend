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

package navigation

import controllers.journeys.expenses._
import controllers.{journeys, standard}
import models._
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.expenses.individualCategories._
import pages._
import pages.expenses.advertisingOrMarketing._
import pages.expenses.construction.{ConstructionIndustryAmountPage, ConstructionIndustryDisallowableAmountPage}
import pages.expenses.depreciation.DepreciationDisallowableAmountPage
import pages.expenses.financialCharges.FinancialChargesAmountPage
import pages.expenses.entertainment.EntertainmentAmountPage
import pages.expenses.goodsToSellOrUse.{DisallowableGoodsToSellOrUseAmountPage, GoodsToSellOrUseAmountPage}
import pages.expenses.interest.{InterestAmountPage, InterestDisallowableAmountPage}
import pages.expenses.officeSupplies.{OfficeSuppliesAmountPage, OfficeSuppliesDisallowableAmountPage}
import pages.expenses.otherExpenses.{OtherExpensesAmountPage, OtherExpensesDisallowableAmountPage}
import pages.expenses.professionalFees.{ProfessionalFeesAmountPage, ProfessionalFeesDisallowableAmountPage}
import pages.expenses.repairsandmaintenance.{RepairsAndMaintenanceAmountPage, RepairsAndMaintenanceDisallowableAmountPage}
import pages.expenses.staffCosts.{StaffCostsAmountPage, StaffCostsDisallowableAmountPage}
import pages.expenses.tailoring.individualCategories._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class ExpensesNavigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => TaxYear => BusinessId => Call = {

    case OfficeSuppliesAmountPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(OfficeSuppliesPage, Some(businessId)) match {
              case Some(OfficeSupplies.YesAllowable) =>
                officeSupplies.routes.OfficeSuppliesCYAController.onPageLoad(taxYear, businessId)
              case Some(OfficeSupplies.YesDisallowable) =>
                officeSupplies.routes.OfficeSuppliesDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)
              case _ =>
                standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case OfficeSuppliesDisallowableAmountPage =>
      _ => taxYear => businessId => officeSupplies.routes.OfficeSuppliesCYAController.onPageLoad(taxYear, businessId)

    case OtherExpensesAmountPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(OtherExpensesPage, Some(businessId)) match {
              case Some(OtherExpenses.YesAllowable) =>
                otherExpenses.routes.OtherExpensesCYAController.onPageLoad(taxYear, businessId)
              case Some(OtherExpenses.YesDisallowable) =>
                journeys.expenses.otherExpenses.routes.OtherExpensesDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)
              case _ =>
                standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case OtherExpensesDisallowableAmountPage =>
      _ => taxYear => businessId => otherExpenses.routes.OtherExpensesCYAController.onPageLoad(taxYear, businessId)

    case FinancialChargesAmountPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            _ =>
              userAnswers.get(DisallowableOtherFinancialChargesPage, Some(businessId)) match {
                case Some(DisallowableOtherFinancialCharges.Yes) =>
                  financialCharges.routes.FinancialChargesDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)
                case Some(DisallowableOtherFinancialCharges.No) =>
                  standard.routes.JourneyRecoveryController.onPageLoad() // TODO: Implement CYA nav in SASS-6688
                case _ =>
                  standard.routes.JourneyRecoveryController.onPageLoad()
              }

          // TODO: Implement FinancialChargesDisallowableAmountPage nav to CYA in SASS-6688

    case GoodsToSellOrUseAmountPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(GoodsToSellOrUsePage, Some(businessId)) match {
              case Some(GoodsToSellOrUse.YesAllowable) =>
                goodsToSellOrUse.routes.GoodsToSellOrUseCYAController.onPageLoad(taxYear, businessId)
              case Some(GoodsToSellOrUse.YesDisallowable) =>
                goodsToSellOrUse.routes.DisallowableGoodsToSellOrUseAmountController.onPageLoad(taxYear, businessId, NormalMode)
              case _ => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case DisallowableGoodsToSellOrUseAmountPage =>
      _ => taxYear => businessId => goodsToSellOrUse.routes.GoodsToSellOrUseCYAController.onPageLoad(taxYear, businessId)

    case RepairsAndMaintenanceAmountPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(RepairsAndMaintenancePage, Some(businessId)) match {
              case Some(RepairsAndMaintenance.YesDisallowable) =>
                repairsandmaintenance.routes.RepairsAndMaintenanceDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)
              case Some(RepairsAndMaintenance.YesAllowable) =>
                repairsandmaintenance.routes.RepairsAndMaintenanceCostsCYAController.onPageLoad(taxYear, businessId)
              case _ => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case RepairsAndMaintenanceDisallowableAmountPage =>
      _ => taxYear => businessId => repairsandmaintenance.routes.RepairsAndMaintenanceCostsCYAController.onPageLoad(taxYear, businessId)

    case AdvertisingOrMarketingAmountPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(AdvertisingOrMarketingPage, Some(businessId)) match {
              case Some(AdvertisingOrMarketing.YesDisallowable) =>
                advertisingOrMarketing.routes.AdvertisingDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)
              case Some(AdvertisingOrMarketing.YesAllowable) => advertisingOrMarketing.routes.AdvertisingCYAController.onPageLoad(taxYear, businessId)
              case _                                         => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case AdvertisingOrMarketingDisallowableAmountPage =>
      _ => taxYear => businessId => advertisingOrMarketing.routes.AdvertisingCYAController.onPageLoad(taxYear, businessId)

    case EntertainmentAmountPage =>
      _ => taxYear => businessId => entertainment.routes.EntertainmentCYAController.onPageLoad(taxYear, businessId)

    case ConstructionIndustryAmountPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(DisallowableSubcontractorCostsPage, Some(businessId)) match {
              case Some(DisallowableSubcontractorCosts.Yes) =>
                construction.routes.ConstructionIndustryDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)
              case Some(DisallowableSubcontractorCosts.No) =>
                construction.routes.ConstructionIndustryCYAController.onPageLoad(taxYear, businessId)
              case None => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case ConstructionIndustryDisallowableAmountPage =>
      _ => taxYear => businessId => construction.routes.ConstructionIndustryCYAController.onPageLoad(taxYear, businessId)

    case StaffCostsAmountPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(DisallowableStaffCostsPage, Some(businessId)) match {
              case Some(DisallowableStaffCosts.Yes) =>
                staffCosts.routes.StaffCostsDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)
              case Some(DisallowableStaffCosts.No) =>
                staffCosts.routes.StaffCostsCYAController.onPageLoad(taxYear, businessId)
              case _ => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case StaffCostsDisallowableAmountPage =>
      _ => taxYear => businessId => staffCosts.routes.StaffCostsCYAController.onPageLoad(taxYear, businessId)

    case ProfessionalFeesAmountPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(DisallowableProfessionalFeesPage, Some(businessId)) match {
              case Some(DisallowableProfessionalFees.Yes) =>
                professionalFees.routes.ProfessionalFeesDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)
              case Some(DisallowableProfessionalFees.No) =>
                professionalFees.routes.ProfessionalFeesCYAController.onPageLoad(taxYear, businessId)
              case None => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case ProfessionalFeesDisallowableAmountPage =>
      _ => taxYear => businessId => professionalFees.routes.ProfessionalFeesCYAController.onPageLoad(taxYear, businessId)

    case InterestAmountPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(DisallowableInterestPage, Some(businessId)) match {
              case Some(DisallowableInterest.Yes) =>
                interest.routes.InterestDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)
              case Some(DisallowableInterest.No) =>
                interest.routes.InterestDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)
              case None => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case InterestDisallowableAmountPage =>
      _ => taxYear => businessId => professionalFees.routes.ProfessionalFeesCYAController.onPageLoad(taxYear, businessId)

    case DepreciationDisallowableAmountPage =>
      _ => taxYear => businessId => depreciation.routes.DepreciationCYAController.onPageLoad(taxYear, businessId)

    case _ => _ => _ => _ => standard.routes.JourneyRecoveryController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => TaxYear => BusinessId => Call = {

    case OfficeSuppliesAmountPage | OfficeSuppliesDisallowableAmountPage =>
      _ => taxYear => businessId => officeSupplies.routes.OfficeSuppliesCYAController.onPageLoad(taxYear, businessId)

    case OtherExpensesAmountPage | OtherExpensesDisallowableAmountPage =>
      _ => taxYear => businessId => otherExpenses.routes.OtherExpensesCYAController.onPageLoad(taxYear, businessId)

    case GoodsToSellOrUseAmountPage | DisallowableGoodsToSellOrUseAmountPage =>
      _ => taxYear => businessId => goodsToSellOrUse.routes.GoodsToSellOrUseCYAController.onPageLoad(taxYear, businessId)

    case AdvertisingOrMarketingAmountPage =>
      _ => taxYear => businessId => advertisingOrMarketing.routes.AdvertisingCYAController.onPageLoad(taxYear, businessId)

    case AdvertisingOrMarketingDisallowableAmountPage =>
      _ => taxYear => businessId => advertisingOrMarketing.routes.AdvertisingCYAController.onPageLoad(taxYear, businessId)

    case EntertainmentAmountPage =>
      _ => taxYear => businessId => entertainment.routes.EntertainmentCYAController.onPageLoad(taxYear, businessId)

    case ConstructionIndustryAmountPage =>
      _ => taxYear => businessId => construction.routes.ConstructionIndustryCYAController.onPageLoad(taxYear, businessId)

    case ConstructionIndustryDisallowableAmountPage =>
      _ => taxYear => businessId => construction.routes.ConstructionIndustryCYAController.onPageLoad(taxYear, businessId)

    case ProfessionalFeesAmountPage =>
      _ => taxYear => businessId => professionalFees.routes.ProfessionalFeesCYAController.onPageLoad(taxYear, businessId)

    case ProfessionalFeesDisallowableAmountPage =>
      _ => taxYear => businessId => professionalFees.routes.ProfessionalFeesCYAController.onPageLoad(taxYear, businessId)

    case RepairsAndMaintenanceAmountPage | RepairsAndMaintenanceDisallowableAmountPage =>
      _ => taxYear => businessId => repairsandmaintenance.routes.RepairsAndMaintenanceCostsCYAController.onPageLoad(taxYear, businessId)

    case StaffCostsAmountPage =>
      _ => taxYear => businessId => staffCosts.routes.StaffCostsCYAController.onPageLoad(taxYear, businessId)

    case StaffCostsDisallowableAmountPage =>
      _ => taxYear => businessId => staffCosts.routes.StaffCostsCYAController.onPageLoad(taxYear, businessId)

    case DepreciationDisallowableAmountPage =>
      _ => taxYear => businessId => depreciation.routes.DepreciationCYAController.onPageLoad(taxYear, businessId)

    case _ => _ => _ => _ => standard.routes.JourneyRecoveryController.onPageLoad()

  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId): Call =
    mode match {
      case NormalMode => normalRoutes(page)(userAnswers)(taxYear)(businessId)
      case CheckMode  => checkRouteMap(page)(userAnswers)(taxYear)(businessId)
    }

}
