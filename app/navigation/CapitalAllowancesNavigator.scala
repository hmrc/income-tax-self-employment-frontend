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

package navigation

import cats.implicits.catsSyntaxOptionId
import controllers.journeys.capitalallowances._
import controllers.standard
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.capitalallowances.ZecAllowance
import models.journeys.capitalallowances.electricVehicleChargePoints._
import models.journeys.capitalallowances.zeroEmissionCars.ZecOnlyForSelfEmployment
import models.{CheckMode, Mode, NormalMode}
import pages.Page
import pages.capitalallowances.balancingAllowance.BalancingAllowancePage
import pages.capitalallowances.electricVehicleChargePoints._
import pages.capitalallowances.tailoring.{ClaimCapitalAllowancesPage, SelectCapitalAllowancesPage}
import pages.capitalallowances.zeroEmissionCars._
import pages.capitalallowances.zeroEmissionGoodsVehicle._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class CapitalAllowancesNavigator @Inject() {

  private val normalRoutes: Page => UserAnswers => TaxYear => BusinessId => Call = {

    // *** Tailoring ***

    case ClaimCapitalAllowancesPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(ClaimCapitalAllowancesPage, businessId.some) match {
              case Some(true)  => tailoring.routes.SelectCapitalAllowancesController.onPageLoad(taxYear, businessId, NormalMode)
              case Some(false) => tailoring.routes.CapitalAllowanceCYAController.onPageLoad(taxYear, businessId)
              case _           => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case SelectCapitalAllowancesPage =>
      _ =>
        taxYear =>
          businessId =>
            tailoring.routes.CapitalAllowanceCYAController.onPageLoad(taxYear, businessId)

        // *** Zero Emission Cars ***

    case ZeroEmissionCarsPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(ZeroEmissionCarsPage, Some(businessId)) match {
              case Some(true)  => zeroEmissionCars.routes.ZecAllowanceController.onPageLoad(taxYear, businessId, NormalMode)
              case Some(false) => zeroEmissionCars.routes.ZeroEmissionCarsCYAController.onPageLoad(taxYear, businessId)
              case _           => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case ZecAllowancePage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(ZecAllowancePage, Some(businessId)) match {
              case Some(ZecAllowance.Yes) =>
                zeroEmissionCars.routes.ZecTotalCostOfCarController.onPageLoad(taxYear, businessId, NormalMode)
              case Some(ZecAllowance.No) => zeroEmissionCars.routes.ZeroEmissionCarsCYAController.onPageLoad(taxYear, businessId)
              case _                     => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case ZecTotalCostOfCarPage =>
      _ => taxYear => businessId => zeroEmissionCars.routes.ZecOnlyForSelfEmploymentController.onPageLoad(taxYear, businessId, NormalMode)

    case ZecOnlyForSelfEmploymentPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(ZecOnlyForSelfEmploymentPage, Some(businessId)) match {
              case Some(ZecOnlyForSelfEmployment.No) =>
                zeroEmissionCars.routes.ZecUseOutsideSEController.onPageLoad(taxYear, businessId, NormalMode)
              case Some(ZecOnlyForSelfEmployment.Yes) =>
                zeroEmissionCars.routes.ZecHowMuchDoYouWantToClaimController.onPageLoad(taxYear, businessId, NormalMode)
              case _ => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case ZecUseOutsideSEPage =>
      _ => taxYear => businessId => zeroEmissionCars.routes.ZecHowMuchDoYouWantToClaimController.onPageLoad(taxYear, businessId, NormalMode)

    case ZecHowMuchDoYouWantToClaimPage =>
      _ =>
        taxYear =>
          businessId =>
            zeroEmissionCars.routes.ZeroEmissionCarsCYAController.onPageLoad(taxYear, businessId)

        // *** Zero Emission Goods Vehicles ***

    case ZeroEmissionGoodsVehiclePage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(ZeroEmissionGoodsVehiclePage, Some(businessId)) match {
              case Some(true) =>
                zeroEmissionGoodsVehicle.routes.ZegvAllowanceController.onPageLoad(taxYear, businessId, NormalMode)
              case Some(false) =>
                zeroEmissionGoodsVehicle.routes.ZeroEmissionGoodsVehicleCYAController.onPageLoad(taxYear, businessId)
              case _ => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case ZegvTotalCostOfVehiclePage =>
      _ =>
        taxYear =>
          businessId =>
            zeroEmissionGoodsVehicle.routes.ZeroEmissionGoodsVehicleCYAController.onPageLoad(taxYear, businessId)

        // *** Electric Vehicle Charge Points ***

    case EVCPAllowancePage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(EVCPAllowancePage, Some(businessId)) match {
              case Some(true) =>
                electricVehicleChargePoints.routes.ChargePointTaxReliefController.onPageLoad(taxYear, businessId, NormalMode)
              case Some(false) =>
                electricVehicleChargePoints.routes.ElectricVehicleChargePointsCYAController.onPageLoad(taxYear, businessId)
              case _ => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case ChargePointTaxReliefPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(ChargePointTaxReliefPage, Some(businessId)) match {
              case Some(true) =>
                electricVehicleChargePoints.routes.AmountSpentOnEvcpController.onPageLoad(taxYear, businessId, NormalMode)
              case Some(false) =>
                electricVehicleChargePoints.routes.ElectricVehicleChargePointsCYAController.onPageLoad(taxYear, businessId)
              case _ => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case AmountSpentOnEvcpPage =>
      _ => taxYear => businessId => electricVehicleChargePoints.routes.EvcpOnlyForSelfEmploymentController.onPageLoad(taxYear, businessId, NormalMode)

    case EvcpOnlyForSelfEmploymentPage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(EvcpOnlyForSelfEmploymentPage, Some(businessId)) match {
              case Some(EvcpOnlyForSelfEmployment.Yes) =>
                electricVehicleChargePoints.routes.EvcpHowMuchDoYouWantToClaimController.onPageLoad(taxYear, businessId, NormalMode)
              case Some(EvcpOnlyForSelfEmployment.No) =>
                electricVehicleChargePoints.routes.EvcpUseOutsideSEController.onPageLoad(taxYear, businessId, NormalMode)
              case _ => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case EvcpUseOutsideSEPage =>
      _ =>
        taxYear => businessId => electricVehicleChargePoints.routes.EvcpHowMuchDoYouWantToClaimController.onPageLoad(taxYear, businessId, NormalMode)

    case EvcpHowMuchDoYouWantToClaimPage =>
      _ => taxYear => businessId => electricVehicleChargePoints.routes.ElectricVehicleChargePointsCYAController.onPageLoad(taxYear, businessId)

    case BalancingAllowancePage =>
      userAnswers =>
        taxYear =>
          businessId =>
            userAnswers.get(BalancingAllowancePage, Some(businessId)) match {
              case Some(true) =>
                balancingAllowance.routes.BalancingAllowanceController.onPageLoad(
                  taxYear,
                  businessId,
                  NormalMode
                ) // TODO: change to balancing allowance amount page
              case Some(false) =>
                balancingAllowance.routes.BalancingAllowanceController.onPageLoad(
                  taxYear,
                  businessId,
                  NormalMode
                ) // TODO: change to balancing allowance CYA
              case _ => standard.routes.JourneyRecoveryController.onPageLoad()
            }

    case _ => _ => _ => _ => standard.routes.JourneyRecoveryController.onPageLoad()
  }

  private val checkRoutes: Page => UserAnswers => TaxYear => BusinessId => Call = {

    case ClaimCapitalAllowancesPage | SelectCapitalAllowancesPage =>
      _ => taxYear => businessId => tailoring.routes.CapitalAllowanceCYAController.onPageLoad(taxYear, businessId)

    case ZeroEmissionCarsPage | ZecAllowancePage | ZecTotalCostOfCarPage | ZecOnlyForSelfEmploymentPage | ZecUseOutsideSEPage |
        ZecHowMuchDoYouWantToClaimPage =>
      _ => taxYear => businessId => zeroEmissionCars.routes.ZeroEmissionCarsCYAController.onPageLoad(taxYear, businessId)

    case ZeroEmissionGoodsVehiclePage | ZegvAllowancePage | ZegvTotalCostOfVehiclePage =>
      _ => taxYear => businessId => zeroEmissionGoodsVehicle.routes.ZeroEmissionGoodsVehicleCYAController.onPageLoad(taxYear, businessId)

    case EVCPAllowancePage | ChargePointTaxReliefPage | AmountSpentOnEvcpPage | EvcpOnlyForSelfEmploymentPage | EvcpUseOutsideSEPage |
        EvcpHowMuchDoYouWantToClaimPage =>
      _ => taxYear => businessId => electricVehicleChargePoints.routes.ElectricVehicleChargePointsCYAController.onPageLoad(taxYear, businessId)

    case BalancingAllowancePage =>
      _ => taxYear => businessId => balancingAllowance.routes.BalancingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)

    case _ =>
      _ => _ => _ => standard.routes.JourneyRecoveryController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId): Call =
    mode match {
      case NormalMode => normalRoutes(page)(userAnswers)(taxYear)(businessId)
      case CheckMode  => checkRoutes(page)(userAnswers)(taxYear)(businessId)
    }
}
