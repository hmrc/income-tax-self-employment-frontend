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
import models.journeys.capitalallowances.ZeroEmissionCarsAllowance
import models.journeys.capitalallowances.zeroEmissionCars.ZecOnlyForSelfEmployment
import models.{CheckMode, Mode, NormalMode}
import pages.Page
import pages.capitalallowances.tailoring.{ClaimCapitalAllowancesPage, SelectCapitalAllowancesPage}
import pages.capitalallowances.zeroEmissionCars._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class CapitalAllowancesNavigator @Inject() {

  private def normalRoutes(page: Page)(implicit userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId): Call = page match {

    case ClaimCapitalAllowancesPage =>
      userAnswers.get(ClaimCapitalAllowancesPage, businessId.some) match {
        case Some(true)  => tailoring.routes.SelectCapitalAllowancesController.onPageLoad(taxYear, businessId, NormalMode)
        case Some(false) => tailoring.routes.CapitalAllowanceCYAController.onPageLoad(taxYear, businessId)
        case _           => standard.routes.JourneyRecoveryController.onPageLoad()
      }

    case SelectCapitalAllowancesPage =>
      tailoring.routes.CapitalAllowanceCYAController.onPageLoad(taxYear, businessId)

    case ZecUsedForWorkPage =>
      userAnswers.get(ZecUsedForWorkPage, Some(businessId)) match {
        case Some(true)  => zeroEmissionCars.routes.ZecAllowanceController.onPageLoad(taxYear, businessId, NormalMode)
        case Some(false) => zeroEmissionCars.routes.ZeroEmissionCarsCYAController.onPageLoad(taxYear, businessId)
        case _           => standard.routes.JourneyRecoveryController.onPageLoad()
      }

    case ZecAllowancePage =>
      userAnswers.get(ZecAllowancePage, Some(businessId)) match {
        case Some(ZeroEmissionCarsAllowance.Yes) =>
          zeroEmissionCars.routes.ZecTotalCostOfCarController.onPageLoad(taxYear, businessId, NormalMode)
        case Some(ZeroEmissionCarsAllowance.No) => zeroEmissionCars.routes.ZeroEmissionCarsCYAController.onPageLoad(taxYear, businessId)
        case _                                  => standard.routes.JourneyRecoveryController.onPageLoad()
      }

    case ZecTotalCostOfCarPage =>
      zeroEmissionCars.routes.ZecUsedForSelfEmploymentController.onPageLoad(taxYear, businessId, NormalMode)

    case ZecOnlyForSelfEmploymentPage =>
      userAnswers.get(ZecOnlyForSelfEmploymentPage, Some(businessId)) match {
        case Some(ZecOnlyForSelfEmployment.No) =>
          zeroEmissionCars.routes.ZecUseOutsideSEController.onPageLoad(taxYear, businessId, NormalMode)
        case Some(ZecOnlyForSelfEmployment.Yes) =>
          zeroEmissionCars.routes.ZecHowMuchDoYouWantToClaimController.onPageLoad(taxYear, businessId, NormalMode)
        case _ => standard.routes.JourneyRecoveryController.onPageLoad()
      }

    case ZecUseOutsideSEPage =>
      zeroEmissionCars.routes.ZecHowMuchDoYouWantToClaimController.onPageLoad(taxYear, businessId, NormalMode)

    case ZecHowMuchDoYouWantToClaimPage =>
      zeroEmissionCars.routes.ZeroEmissionCarsCYAController.onPageLoad(taxYear, businessId)

    case _ =>
      standard.routes.JourneyRecoveryController.onPageLoad()
  }

  private val checkRoutes: Page => UserAnswers => TaxYear => BusinessId => Call = {

    case ClaimCapitalAllowancesPage | SelectCapitalAllowancesPage =>
      _ => taxYear => businessId => tailoring.routes.CapitalAllowanceCYAController.onPageLoad(taxYear, businessId)

    case ZecUsedForWorkPage | ZecAllowancePage | ZecTotalCostOfCarPage | ZecOnlyForSelfEmploymentPage | ZecUseOutsideSEPage |
        ZecHowMuchDoYouWantToClaimPage =>
      _ => taxYear => businessId => zeroEmissionCars.routes.ZeroEmissionCarsCYAController.onPageLoad(taxYear, businessId)

    case _ =>
      _ => _ => _ => standard.routes.JourneyRecoveryController.onPageLoad()
  }

  def nextPage(implicit page: Page, mode: Mode, userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId): Call =
    mode match {
      case NormalMode => normalRoutes(page)
      case CheckMode  => checkRoutes(page)(userAnswers)(taxYear)(businessId)
    }
}
