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

import controllers.journeys.expenses.{entertainment, goodsToSellOrUse, officeSupplies, repairsandmaintenance}
import controllers.{journeys, standard}
import models._
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.Journey.{ExpensesGoodsToSellOrUse, ExpensesOfficeSupplies, ExpensesRepairsAndMaintenance}
import models.journeys.expenses.{GoodsToSellOrUse, OfficeSupplies, RepairsAndMaintenance}
import pages._
import pages.expenses.entertainment.EntertainmentAmountPage
import pages.expenses.goodsToSellOrUse.{DisallowableGoodsToSellOrUseAmountPage, GoodsToSellOrUseAmountPage, GoodsToSellOrUseCYAPage}
import pages.expenses.officeSupplies.{OfficeSuppliesAmountPage, OfficeSuppliesCYAPage, OfficeSuppliesDisallowableAmountPage}
import pages.expenses.repairsandmaintenance.{
  RepairsAndMaintenanceAmountPage,
  RepairsAndMaintenanceCostsCYAPage,
  RepairsAndMaintenanceDisallowableAmountPage
}
import pages.expenses.tailoring.{GoodsToSellOrUsePage, OfficeSuppliesPage, RepairsAndMaintenancePage}
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class ExpensesNavigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Int => String => Call = {

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

    case OfficeSuppliesCYAPage =>
      _ =>
        taxYear =>
          businessId => journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, businessId, ExpensesOfficeSupplies.toString, NormalMode)

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

    case GoodsToSellOrUseCYAPage =>
      _ =>
        taxYear =>
          businessId => journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, businessId, ExpensesGoodsToSellOrUse.toString, NormalMode)

    case RepairsAndMaintenanceAmountPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(RepairsAndMaintenancePage, Some(businessId)) match {
              case Some(RepairsAndMaintenance.YesAllowable) =>
                repairsandmaintenance.routes.RepairsAndMaintenanceCostsCYAController.onPageLoad(TaxYear(taxYear), BusinessId(businessId))
              case Some(RepairsAndMaintenance.YesDisallowable) =>
                repairsandmaintenance.routes.RepairsAndMaintenanceDisallowableAmountController.onPageLoad(
                  TaxYear(taxYear),
                  BusinessId(businessId),
                  NormalMode)
              case _ => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case EntertainmentAmountPage =>
      _ =>
        taxYear =>
          businessId =>
            entertainment.routes.EntertainmentAmountController.onPageLoad(
              TaxYear(taxYear),
              BusinessId(businessId),
              NormalMode
            ) // TODO to CYA page when created

    case RepairsAndMaintenanceDisallowableAmountPage =>
      _ =>
        taxYear =>
          businessId => repairsandmaintenance.routes.RepairsAndMaintenanceCostsCYAController.onPageLoad(TaxYear(taxYear), BusinessId(businessId))

    case RepairsAndMaintenanceCostsCYAPage =>
      _ =>
        taxYear =>
          businessId =>
            journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, businessId, ExpensesRepairsAndMaintenance.toString, NormalMode)

    case _ => _ => _ => _ => standard.routes.JourneyRecoveryController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Int => String => Call = {

    case OfficeSuppliesAmountPage | OfficeSuppliesDisallowableAmountPage =>
      _ => taxYear => businessId => officeSupplies.routes.OfficeSuppliesCYAController.onPageLoad(taxYear, businessId)

    case GoodsToSellOrUseAmountPage | DisallowableGoodsToSellOrUseAmountPage =>
      _ => taxYear => businessId => goodsToSellOrUse.routes.GoodsToSellOrUseCYAController.onPageLoad(taxYear, businessId)

    case EntertainmentAmountPage =>
      _ =>
        taxYear =>
          businessId =>
            entertainment.routes.EntertainmentAmountController.onPageLoad(
              TaxYear(taxYear),
              BusinessId(businessId),
              CheckMode
            ) // TODO to CYA page when created

    case RepairsAndMaintenanceAmountPage | RepairsAndMaintenanceDisallowableAmountPage =>
      _ =>
        taxYear =>
          businessId => repairsandmaintenance.routes.RepairsAndMaintenanceCostsCYAController.onPageLoad(TaxYear(taxYear), BusinessId(businessId))

    case _ => _ => _ => _ => standard.routes.JourneyRecoveryController.onPageLoad()

  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, taxYear: Int, businessId: String): Call =
    mode match {
      case NormalMode => normalRoutes(page)(userAnswers)(taxYear)(businessId)
      case CheckMode  => checkRouteMap(page)(userAnswers)(taxYear)(businessId)
    }

  /** User also for CYA pages
    */
  def nextNormalRoute(sourcePage: Page, userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId): Call =
    normalRoutes(sourcePage)(userAnswers)(taxYear.value)(businessId.value)

}
