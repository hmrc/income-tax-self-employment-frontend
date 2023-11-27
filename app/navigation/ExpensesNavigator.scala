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
import models.common.AccountingType.{Accrual, Cash}
import models.common.{AccountingType, BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.Journey.{
  ExpensesEntertainment,
  ExpensesGoodsToSellOrUse,
  ExpensesOfficeSupplies,
  ExpensesRepairsAndMaintenance,
  ExpensesStaffCosts
}
import models.journeys.expenses.{DisallowableStaffCosts, GoodsToSellOrUse, OfficeSupplies, RepairsAndMaintenance}
import pages._
import pages.expenses.entertainment.{EntertainmentAmountPage, EntertainmentCYAPage}
import pages.expenses.goodsToSellOrUse.{DisallowableGoodsToSellOrUseAmountPage, GoodsToSellOrUseAmountPage, GoodsToSellOrUseCYAPage}
import pages.expenses.officeSupplies.{OfficeSuppliesAmountPage, OfficeSuppliesCYAPage, OfficeSuppliesDisallowableAmountPage}
import pages.expenses.repairsandmaintenance.{
  RepairsAndMaintenanceAmountPage,
  RepairsAndMaintenanceCostsCYAPage,
  RepairsAndMaintenanceDisallowableAmountPage
}
import pages.expenses.staffCosts.{StaffCostsAmountPage, StaffCostsCYAPage, StaffCostsDisallowableAmountPage}
import pages.expenses.tailoring.{DisallowableStaffCostsPage, GoodsToSellOrUsePage, OfficeSuppliesPage, RepairsAndMaintenancePage}
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class ExpensesNavigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => TaxYear => BusinessId => Option[AccountingType] => Call = {

    case OfficeSuppliesAmountPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            _ =>
              userAnswers.get(OfficeSuppliesPage, Some(businessId.value)) match {
                case Some(OfficeSupplies.YesAllowable) =>
                  officeSupplies.routes.OfficeSuppliesCYAController.onPageLoad(taxYear, businessId.value)
                case Some(OfficeSupplies.YesDisallowable) =>
                  officeSupplies.routes.OfficeSuppliesDisallowableAmountController.onPageLoad(taxYear, businessId.value, NormalMode)
                case _ =>
                  standard.routes.JourneyRecoveryController.onPageLoad()
              }

    case OfficeSuppliesDisallowableAmountPage =>
      _ => taxYear => businessId => _ => officeSupplies.routes.OfficeSuppliesCYAController.onPageLoad(taxYear, businessId.value)

    case OfficeSuppliesCYAPage =>
      _ =>
        taxYear =>
          businessId =>
            _ => journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, businessId.value, ExpensesOfficeSupplies.toString, NormalMode)

    case GoodsToSellOrUseAmountPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            _ =>
              userAnswers.get(GoodsToSellOrUsePage, Some(businessId.value)) match {
                case Some(GoodsToSellOrUse.YesAllowable) =>
                  goodsToSellOrUse.routes.GoodsToSellOrUseCYAController.onPageLoad(taxYear, businessId.value)
                case Some(GoodsToSellOrUse.YesDisallowable) =>
                  goodsToSellOrUse.routes.DisallowableGoodsToSellOrUseAmountController.onPageLoad(taxYear, businessId.value, NormalMode)
                case _ => standard.routes.JourneyRecoveryController.onPageLoad()
              }

    case DisallowableGoodsToSellOrUseAmountPage =>
      _ => taxYear => businessId => _ => goodsToSellOrUse.routes.GoodsToSellOrUseCYAController.onPageLoad(taxYear, businessId.value)

    case GoodsToSellOrUseCYAPage =>
      _ =>
        taxYear =>
          businessId =>
            _ => journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, businessId.value, ExpensesGoodsToSellOrUse.toString, NormalMode)

    case RepairsAndMaintenanceAmountPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            _ =>
              userAnswers.get(RepairsAndMaintenancePage, Some(businessId.value)) match {
                case Some(RepairsAndMaintenance.YesAllowable) =>
                  repairsandmaintenance.routes.RepairsAndMaintenanceCostsCYAController.onPageLoad(taxYear, businessId)
                case Some(RepairsAndMaintenance.YesDisallowable) =>
                  repairsandmaintenance.routes.RepairsAndMaintenanceDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)
                case _ => standard.routes.JourneyRecoveryController.onPageLoad()
              }

    case RepairsAndMaintenanceDisallowableAmountPage =>
      _ => taxYear => businessId => _ => repairsandmaintenance.routes.RepairsAndMaintenanceCostsCYAController.onPageLoad(taxYear, businessId)

    case RepairsAndMaintenanceCostsCYAPage =>
      _ =>
        taxYear =>
          businessId =>
            _ =>
              journeys.routes.SectionCompletedStateController.onPageLoad(
                taxYear,
                businessId.value,
                ExpensesRepairsAndMaintenance.toString,
                NormalMode)

    case EntertainmentAmountPage =>
      _ => taxYear => businessId => _ => entertainment.routes.EntertainmentCYAController.onPageLoad(taxYear, businessId)

    case EntertainmentCYAPage =>
      _ =>
        taxYear =>
          businessId =>
            _ => journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, businessId.value, ExpensesEntertainment.toString, NormalMode)

    case StaffCostsAmountPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            optAccountingType =>
              userAnswers.get(DisallowableStaffCostsPage, Some(businessId.value)) match {
                case Some(DisallowableStaffCosts.Yes) if optAccountingType.getOrElse(Cash) == Accrual =>
                  staffCosts.routes.StaffCostsDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)
                case Some(DisallowableStaffCosts.Yes | DisallowableStaffCosts.No) =>
                  staffCosts.routes.StaffCostsCYAController.onPageLoad(taxYear, businessId)
                case _ => standard.routes.JourneyRecoveryController.onPageLoad()
              }

    case StaffCostsDisallowableAmountPage =>
      _ => taxYear => businessId => _ => staffCosts.routes.StaffCostsCYAController.onPageLoad(taxYear, businessId)

    case StaffCostsCYAPage =>
      _ =>
        taxYear =>
          businessId =>
            _ => journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, businessId.value, ExpensesStaffCosts.toString, NormalMode)

    case _ => _ => _ => _ => _ => standard.routes.JourneyRecoveryController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => TaxYear => BusinessId => Call = {

    case OfficeSuppliesAmountPage | OfficeSuppliesDisallowableAmountPage =>
      _ => taxYear => businessId => officeSupplies.routes.OfficeSuppliesCYAController.onPageLoad(taxYear, businessId.value)

    case GoodsToSellOrUseAmountPage | DisallowableGoodsToSellOrUseAmountPage =>
      _ => taxYear => businessId => goodsToSellOrUse.routes.GoodsToSellOrUseCYAController.onPageLoad(taxYear, businessId.value)

    case EntertainmentAmountPage =>
      _ => taxYear => businessId => entertainment.routes.EntertainmentCYAController.onPageLoad(taxYear, businessId)

    case RepairsAndMaintenanceAmountPage | RepairsAndMaintenanceDisallowableAmountPage =>
      _ => taxYear => businessId => repairsandmaintenance.routes.RepairsAndMaintenanceCostsCYAController.onPageLoad(taxYear, businessId)

    case StaffCostsAmountPage =>
      _ => taxYear => businessId => staffCosts.routes.StaffCostsCYAController.onPageLoad(taxYear, businessId)

    case StaffCostsDisallowableAmountPage =>
      _ => taxYear => businessId => staffCosts.routes.StaffCostsCYAController.onPageLoad(taxYear, businessId)

    case _ => _ => _ => _ => standard.routes.JourneyRecoveryController.onPageLoad()

  }

  def nextPage(page: Page,
               mode: Mode,
               userAnswers: UserAnswers,
               taxYear: TaxYear,
               businessId: String,
               accountingType: Option[AccountingType] = None): Call =
    mode match {
      case NormalMode => normalRoutes(page)(userAnswers)(taxYear)(BusinessId(businessId))(accountingType)
      case CheckMode  => checkRouteMap(page)(userAnswers)(taxYear)(BusinessId(businessId))
    }

  /** User also for CYA pages
    */
  def nextNormalRoute(sourcePage: Page,
                      userAnswers: UserAnswers,
                      taxYear: TaxYear,
                      businessId: BusinessId,
                      accountingType: Option[AccountingType] = None): Call =
    normalRoutes(sourcePage)(userAnswers)(taxYear)(businessId)(accountingType)

}
