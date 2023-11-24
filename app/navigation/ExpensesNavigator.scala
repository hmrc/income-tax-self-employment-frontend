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
import models.journeys.Journey.{ExpensesEntertainment, ExpensesGoodsToSellOrUse, ExpensesOfficeSupplies, ExpensesRepairsAndMaintenance}
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
import pages.expenses.staffCosts.StaffCostsAmountPage
import pages.expenses.tailoring.{DisallowableStaffCostsPage, GoodsToSellOrUsePage, OfficeSuppliesPage, RepairsAndMaintenancePage}
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class ExpensesNavigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => TaxYear => BusinessId => Call = {

    case OfficeSuppliesAmountPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(OfficeSuppliesPage, Some(businessId.value)) match {
              case Some(OfficeSupplies.YesAllowable) =>
                officeSupplies.routes.OfficeSuppliesCYAController.onPageLoad(taxYear.value, businessId.value)
              case Some(OfficeSupplies.YesDisallowable) =>
                officeSupplies.routes.OfficeSuppliesDisallowableAmountController.onPageLoad(taxYear.value, businessId.value, NormalMode)
              case _ =>
                standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case OfficeSuppliesDisallowableAmountPage =>
      _ => taxYear => businessId => officeSupplies.routes.OfficeSuppliesCYAController.onPageLoad(taxYear.value, businessId.value)

    case OfficeSuppliesCYAPage =>
      _ =>
        taxYear =>
          businessId =>
            journeys.routes.SectionCompletedStateController.onPageLoad(taxYear.value, businessId.value, ExpensesOfficeSupplies.toString, NormalMode)

    case GoodsToSellOrUseAmountPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(GoodsToSellOrUsePage, Some(businessId.value)) match {
              case Some(GoodsToSellOrUse.YesAllowable) =>
                goodsToSellOrUse.routes.GoodsToSellOrUseCYAController.onPageLoad(taxYear.value, businessId.value)
              case Some(GoodsToSellOrUse.YesDisallowable) =>
                goodsToSellOrUse.routes.DisallowableGoodsToSellOrUseAmountController.onPageLoad(taxYear.value, businessId.value, NormalMode)
              case _ => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case DisallowableGoodsToSellOrUseAmountPage =>
      _ => taxYear => businessId => goodsToSellOrUse.routes.GoodsToSellOrUseCYAController.onPageLoad(taxYear.value, businessId.value)

    case GoodsToSellOrUseCYAPage =>
      _ =>
        taxYear =>
          businessId =>
            journeys.routes.SectionCompletedStateController.onPageLoad(taxYear.value, businessId.value, ExpensesGoodsToSellOrUse.toString, NormalMode)

    case RepairsAndMaintenanceAmountPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(RepairsAndMaintenancePage, Some(businessId.value)) match {
              case Some(RepairsAndMaintenance.YesAllowable) =>
                repairsandmaintenance.routes.RepairsAndMaintenanceCostsCYAController.onPageLoad(taxYear, businessId)
              case Some(RepairsAndMaintenance.YesDisallowable) =>
                repairsandmaintenance.routes.RepairsAndMaintenanceDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)
              case _ => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case RepairsAndMaintenanceDisallowableAmountPage =>
      _ => taxYear => businessId => repairsandmaintenance.routes.RepairsAndMaintenanceCostsCYAController.onPageLoad(taxYear, businessId)

    case RepairsAndMaintenanceCostsCYAPage =>
      _ =>
        taxYear =>
          businessId =>
            journeys.routes.SectionCompletedStateController.onPageLoad(
              taxYear.value,
              businessId.value,
              ExpensesRepairsAndMaintenance.toString,
              NormalMode)

    case EntertainmentAmountPage =>
      _ => taxYear => businessId => entertainment.routes.EntertainmentCYAController.onPageLoad(taxYear, businessId)

    case EntertainmentCYAPage =>
      _ =>
        taxYear =>
          businessId =>
            journeys.routes.SectionCompletedStateController.onPageLoad(taxYear.value, businessId.value, ExpensesEntertainment.toString, NormalMode)

    case StaffCostsAmountPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(DisallowableStaffCostsPage, Some(businessId.value)) match {
              case Some(DisallowableStaffCosts.Yes) =>
                staffCosts.routes.StaffCostsAmountController.onPageLoad(taxYear, businessId, NormalMode) // TODO to disallowable page when created
              case Some(DisallowableStaffCosts.No) =>
                staffCosts.routes.StaffCostsAmountController.onPageLoad(taxYear, businessId, NormalMode) // TODO to CYA page when created
              case _ => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case _ => _ => _ => _ => standard.routes.JourneyRecoveryController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => TaxYear => BusinessId => Call = {

    case OfficeSuppliesAmountPage | OfficeSuppliesDisallowableAmountPage =>
      _ => taxYear => businessId => officeSupplies.routes.OfficeSuppliesCYAController.onPageLoad(taxYear.value, businessId.value)

    case GoodsToSellOrUseAmountPage | DisallowableGoodsToSellOrUseAmountPage =>
      _ => taxYear => businessId => goodsToSellOrUse.routes.GoodsToSellOrUseCYAController.onPageLoad(taxYear.value, businessId.value)

    case EntertainmentAmountPage =>
      _ => taxYear => businessId => entertainment.routes.EntertainmentCYAController.onPageLoad(taxYear, businessId)

    case RepairsAndMaintenanceAmountPage | RepairsAndMaintenanceDisallowableAmountPage =>
      _ => taxYear => businessId => repairsandmaintenance.routes.RepairsAndMaintenanceCostsCYAController.onPageLoad(taxYear, businessId)

    case StaffCostsAmountPage =>
      _ =>
        taxYear =>
          businessId => staffCosts.routes.StaffCostsAmountController.onPageLoad(taxYear, businessId, CheckMode) // TODO to CYA page when created

    case _ => _ => _ => _ => standard.routes.JourneyRecoveryController.onPageLoad()

  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, taxYear: Int, businessId: String): Call =
    mode match {
      case NormalMode => normalRoutes(page)(userAnswers)(TaxYear(taxYear))(BusinessId(businessId))
      case CheckMode  => checkRouteMap(page)(userAnswers)(TaxYear(taxYear))(BusinessId(businessId))
    }

  /** User also for CYA pages
    */
  def nextNormalRoute(sourcePage: Page, userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId): Call =
    normalRoutes(sourcePage)(userAnswers)(taxYear)(businessId)

}
