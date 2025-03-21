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

import controllers.standard.routes._
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.{NormalMode, _}
import pages._
import pages.expenses.travelAndAccommodation.{
  SimplifiedExpensesPage,
  TravelAndAccommodationExpenseTypePage,
  TravelForWorkYourVehiclePage,
  VehicleTypePage
}
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

/** When you change the navigator, remember to change also `next` method in each of the pages.
  */
@Singleton
class TravelAndAccommodationNavigator @Inject() {

  private val normalRoutes: Page => UserAnswers => (TaxYear, BusinessId) => Call = {
    case TravelAndAccommodationExpenseTypePage =>
      _ =>
        (taxYear, businessId) =>
          controllers.journeys.expenses.travelAndAccommodation.routes.TravelForWorkYourVehicleController.onPageLoad(taxYear, businessId, NormalMode)

    case TravelForWorkYourVehiclePage =>
      _ =>
        (taxYear, businessId) =>
          controllers.journeys.expenses.travelAndAccommodation.routes.VehicleTypeController.onPageLoad(taxYear, businessId, NormalMode)

    case VehicleTypePage =>
      _ =>
        (taxYear, businessId) =>
          controllers.journeys.expenses.travelAndAccommodation.routes.SimplifiedExpensesController.onPageLoad(taxYear, businessId, NormalMode)

    case SimplifiedExpensesPage =>
      // TODO - no/false path needs to be done - 'Do you want to calculate a fix rated' page
      _ =>
        (taxYear, businessId) =>
          controllers.journeys.expenses.travelAndAccommodation.routes.UseSimplifiedExpensesController.onPageLoad(taxYear, businessId)

    case _ => _ => (_, _) => JourneyRecoveryController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => (TaxYear, BusinessId) => Call = { case _ =>
    _ => (_, _) => JourneyRecoveryController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId): Call =
    mode match {
      case NormalMode =>
        normalRoutes(page)(userAnswers)(taxYear, businessId)
      case CheckMode =>
        checkRouteMap(page)(userAnswers)(taxYear, businessId)
    }
}
