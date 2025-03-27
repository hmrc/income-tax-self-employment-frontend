/*
 * Copyright 2025 HM Revenue & Customs
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

import controllers.journeys.expenses.travelAndAccommodation.routes
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.{NormalMode, _}
import pages._
import pages.expenses.travelAndAccommodation._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

/** When you change the navigator, remember to change also `next` method in each of the pages.
  */
@Singleton
class TravelAndAccommodationNavigator @Inject() {

  private val normalRoutes: Page => UserAnswers => (TaxYear, BusinessId) => Option[Call] = {

    case TravelAndAccommodationExpenseTypePage =>
      _ =>
        (taxYear, businessId) =>
          Some(
            routes.TravelForWorkYourVehicleController
              .onPageLoad(taxYear, businessId, NormalMode))

    case TravelForWorkYourVehiclePage =>
      _ => (taxYear, businessId) => Some(routes.VehicleTypeController.onPageLoad(taxYear, businessId, NormalMode))

    case VehicleTypePage =>
      _ => (taxYear, businessId) => Some(routes.SimplifiedExpensesController.onPageLoad(taxYear, businessId, NormalMode))

    case UseSimplifiedExpensesPage =>
      _ =>
        (taxYear, businessId) =>
          Some(
            routes.TravelForWorkYourMileageController
              .onPageLoad(taxYear, businessId, NormalMode))

    case SimplifiedExpensesPage =>
      ua => (taxYear, businessId) => handleSimplifiedExpenses(ua, taxYear, businessId, NormalMode)

    case VehicleFlatRateChoicePage =>
      ua => (taxYear, businessId) => handleFlatRateChoice(ua, taxYear, businessId, NormalMode)

    case VehicleExpensesPage =>
      ua => (taxYear, businessId) => handleFlatRateChoice(ua, taxYear, businessId, NormalMode)
    case TravelForWorkYourMileagePage =>
      _ =>
        (taxYear, businessId) =>
          Some(
            routes.YourFlatRateForVehicleExpensesController
              .onPageLoad(taxYear, businessId, NormalMode))
    //    case YourFlatRateForVehicleExpensesPage =>
    //      _ =>
    //        (taxYear, businessId) =>
    //          Some(
    //            controllers.journeys.expenses.travelAndAccommodation.routes.CostsNotCoveredController
    //              .onPageLoad(taxYear, businessId, NormalMode))

    case _ => _ => (_, _) => None
  }

  private def handleSimplifiedExpenses(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, mode: Mode): Option[Call] =
    userAnswers.get(SimplifiedExpensesPage, businessId) map {
      case true => routes.UseSimplifiedExpensesController.onPageLoad(taxYear, businessId)
      case false =>
        routes.VehicleFlatRateChoiceController.onPageLoad(taxYear, businessId, mode)
    }

  private def handleFlatRateChoice(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, mode: Mode): Option[Call] =
    userAnswers.get(VehicleFlatRateChoicePage, businessId) map {
      case true =>
        controllers.journeys.expenses.travelAndAccommodation.routes.TravelForWorkYourMileageController.onPageLoad(taxYear, businessId, mode)
      case false =>
        controllers.journeys.expenses.travelAndAccommodation.routes.VehicleExpensesController.onPageLoad(taxYear, businessId, mode)
    }

  private val checkRouteMap: Page => UserAnswers => (TaxYear, BusinessId) => Call = { case _ =>
    _ => (_, _) => controllers.standard.routes.JourneyRecoveryController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId): Call =
    mode match {
      case NormalMode =>
        normalRoutes(page)(userAnswers)(taxYear, businessId).getOrElse(controllers.standard.routes.JourneyRecoveryController.onPageLoad())
      case CheckMode =>
        checkRouteMap(page)(userAnswers)(taxYear, businessId)
    }
}
