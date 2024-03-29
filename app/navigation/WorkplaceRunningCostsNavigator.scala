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
import controllers.standard
import models._
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.expenses.individualCategories._
import models.journeys.expenses.workplaceRunningCosts.{WfbpFlatRateOrActualCosts, WfhFlatRateOrActualCosts}
import pages._
import pages.expenses.tailoring.individualCategories._
import pages.expenses.workplaceRunningCosts.workingFromBusinessPremises._
import pages.expenses.workplaceRunningCosts.workingFromHome._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class WorkplaceRunningCostsNavigator @Inject() {

  private val normalRoutes: Page => UserAnswers => TaxYear => BusinessId => Call = {

    case MoreThan25HoursPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(MoreThan25HoursPage, Some(businessId)) match {
              case Some(true) =>
                workplaceRunningCosts.workingFromHome.routes.WorkingFromHomeHoursController.onPageLoad(taxYear, businessId, NormalMode)
              case Some(false) =>
                workplaceRunningCosts.workingFromHome.routes.WfhExpensesInfoController.onPageLoad(taxYear, businessId, NormalMode)
              case _ => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case WorkingFromHomeHoursPage =>
      _ =>
        taxYear =>
          businessId => workplaceRunningCosts.workingFromHome.routes.WfhFlatRateOrActualCostsController.onPageLoad(taxYear, businessId, NormalMode)

    case WfhFlatRateOrActualCostsPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            val optWFBP                  = userAnswers.get(WorkFromBusinessPremisesPage, Some(businessId))
            val optFlatRateOrActualCosts = userAnswers.get(WfhFlatRateOrActualCostsPage, Some(businessId))
            (optWFBP, optFlatRateOrActualCosts) match {
              case (_, Some(WfhFlatRateOrActualCosts.ActualCosts)) =>
                workplaceRunningCosts.workingFromHome.routes.WfhExpensesInfoController.onPageLoad(taxYear, businessId, NormalMode)
              case (
                    Some(WorkFromBusinessPremises.YesAllowable | WorkFromBusinessPremises.YesDisallowable),
                    Some(WfhFlatRateOrActualCosts.FlatRate)) =>
                workplaceRunningCosts.workingFromBusinessPremises.routes.LiveAtBusinessPremisesController.onPageLoad(taxYear, businessId, NormalMode)
              case (Some(WorkFromBusinessPremises.No), Some(WfhFlatRateOrActualCosts.FlatRate)) =>
                workplaceRunningCosts.routes.WorkplaceRunningCostsCYAController.onPageLoad(taxYear, businessId)
              case _ => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case WfhExpensesInfoPage =>
      _ =>
        taxYear => businessId => workplaceRunningCosts.workingFromHome.routes.WfhClaimingAmountController.onPageLoad(taxYear, businessId, NormalMode)

    case WfhClaimingAmountPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(WorkFromBusinessPremisesPage, Some(businessId)) match {
              case Some(WorkFromBusinessPremises.YesAllowable | WorkFromBusinessPremises.YesDisallowable) =>
                workplaceRunningCosts.workingFromBusinessPremises.routes.LiveAtBusinessPremisesController.onPageLoad(taxYear, businessId, NormalMode)
              case Some(WorkFromBusinessPremises.No) =>
                workplaceRunningCosts.routes.WorkplaceRunningCostsCYAController.onPageLoad(taxYear, businessId)
              case _ => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case LiveAtBusinessPremisesPage =>
      _ =>
        taxYear =>
          businessId =>
            workplaceRunningCosts.workingFromBusinessPremises.routes.BusinessPremisesAmountController.onPageLoad(taxYear, businessId, NormalMode)

    case BusinessPremisesAmountPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            val optWFBP     = userAnswers.get(WorkFromBusinessPremisesPage, Some(businessId))
            val optLiveAtBP = userAnswers.get(LiveAtBusinessPremisesPage, Some(businessId))
            (optWFBP, optLiveAtBP) match {
              case (Some(WorkFromBusinessPremises.YesDisallowable), _) =>
                workplaceRunningCosts.workingFromBusinessPremises.routes.BusinessPremisesDisallowableAmountController
                  .onPageLoad(taxYear, businessId, NormalMode)
              case (Some(WorkFromBusinessPremises.YesAllowable), Some(true)) =>
                workplaceRunningCosts.workingFromBusinessPremises.routes.PeopleLivingAtBusinessPremisesController
                  .onPageLoad(taxYear, businessId, NormalMode)
              case (Some(WorkFromBusinessPremises.YesAllowable), Some(false)) =>
                workplaceRunningCosts.routes.WorkplaceRunningCostsCYAController.onPageLoad(taxYear, businessId)
              case _ => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case BusinessPremisesDisallowableAmountPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(LiveAtBusinessPremisesPage, Some(businessId)) match {
              case Some(true) =>
                workplaceRunningCosts.workingFromBusinessPremises.routes.PeopleLivingAtBusinessPremisesController
                  .onPageLoad(taxYear, businessId, NormalMode)
              case Some(false) =>
                workplaceRunningCosts.routes.WorkplaceRunningCostsCYAController.onPageLoad(taxYear, businessId)
              case _ => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case PeopleLivingAtBusinessPremisesPage =>
      _ =>
        taxYear =>
          businessId =>
            workplaceRunningCosts.workingFromBusinessPremises.routes.WfbpFlatRateOrActualCostsController.onPageLoad(taxYear, businessId, NormalMode)

    case WfbpFlatRateOrActualCostsPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(WfbpFlatRateOrActualCostsPage, Some(businessId)) match {
              case Some(WfbpFlatRateOrActualCosts.ActualCosts) =>
                workplaceRunningCosts.workingFromBusinessPremises.routes.WfbpClaimingAmountController
                  .onPageLoad(taxYear, businessId, NormalMode)
              case Some(WfbpFlatRateOrActualCosts.FlatRate) =>
                workplaceRunningCosts.routes.WorkplaceRunningCostsCYAController.onPageLoad(taxYear, businessId)
              case _ => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case WfbpClaimingAmountPage =>
      _ => taxYear => businessId => workplaceRunningCosts.routes.WorkplaceRunningCostsCYAController.onPageLoad(taxYear, businessId)

    case _ => _ => _ => _ => standard.routes.JourneyRecoveryController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => TaxYear => BusinessId => Call = {
    case MoreThan25HoursPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(MoreThan25HoursPage, Some(businessId)) match {
              case Some(true) =>
                workplaceRunningCosts.workingFromHome.routes.WorkingFromHomeHoursController.onPageLoad(taxYear, businessId, CheckMode)
              case Some(false) =>
                workplaceRunningCosts.routes.WorkplaceRunningCostsCYAController.onPageLoad(taxYear, businessId)
              case _ =>
                standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case WorkingFromHomeHoursPage =>
      _ => taxYear => businessId => workplaceRunningCosts.routes.WorkplaceRunningCostsCYAController.onPageLoad(taxYear, businessId)

    case WfhFlatRateOrActualCostsPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(WfhFlatRateOrActualCostsPage, Some(businessId)) match {
              case Some(WfhFlatRateOrActualCosts.ActualCosts) =>
                workplaceRunningCosts.workingFromHome.routes.WfhExpensesInfoController.onPageLoad(taxYear, businessId, CheckMode)
              case Some(WfhFlatRateOrActualCosts.FlatRate) =>
                workplaceRunningCosts.routes.WorkplaceRunningCostsCYAController.onPageLoad(taxYear, businessId)
              case _ => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case WfhExpensesInfoPage =>
      _ =>
        taxYear => businessId => workplaceRunningCosts.workingFromHome.routes.WfhClaimingAmountController.onPageLoad(taxYear, businessId, CheckMode)

    case WfhClaimingAmountPage =>
      _ => taxYear => businessId => workplaceRunningCosts.routes.WorkplaceRunningCostsCYAController.onPageLoad(taxYear, businessId)

    case LiveAtBusinessPremisesPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(LiveAtBusinessPremisesPage, Some(businessId)) match {
              case Some(true) =>
                workplaceRunningCosts.workingFromBusinessPremises.routes.BusinessPremisesAmountController.onPageLoad(taxYear, businessId, CheckMode)
              case Some(false) =>
                workplaceRunningCosts.routes.WorkplaceRunningCostsCYAController.onPageLoad(taxYear, businessId)
              case _ => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case BusinessPremisesDisallowableAmountPage =>
      _ => taxYear => businessId => workplaceRunningCosts.routes.WorkplaceRunningCostsCYAController.onPageLoad(taxYear, businessId)

    case BusinessPremisesAmountPage =>
      _ => taxYear => businessId => workplaceRunningCosts.routes.WorkplaceRunningCostsCYAController.onPageLoad(taxYear, businessId)

    case PeopleLivingAtBusinessPremisesPage =>
      _ => taxYear => businessId => workplaceRunningCosts.routes.WorkplaceRunningCostsCYAController.onPageLoad(taxYear, businessId)

    case WfbpFlatRateOrActualCostsPage =>
      _ => taxYear => businessId => workplaceRunningCosts.routes.WorkplaceRunningCostsCYAController.onPageLoad(taxYear, businessId)

    case WfbpClaimingAmountPage =>
      _ => taxYear => businessId => workplaceRunningCosts.routes.WorkplaceRunningCostsCYAController.onPageLoad(taxYear, businessId)

    case _ => _ => _ => _ => standard.routes.JourneyRecoveryController.onPageLoad()

  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId): Call =
    mode match {
      case NormalMode => normalRoutes(page)(userAnswers)(taxYear)(businessId)
      case CheckMode  => checkRouteMap(page)(userAnswers)(taxYear)(businessId)
    }

}
