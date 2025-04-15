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
import models.journeys.expenses.individualCategories.TravelForWork
import models.journeys.expenses.travelAndAccommodation.TravelAndAccommodationExpenseType.PublicTransportAndOtherAccommodation
import models.journeys.expenses.travelAndAccommodation.{TravelAndAccommodationExpenseType, YourFlatRateForVehicleExpenses}
import models.{NormalMode, _}
import pages._
import pages.expenses.tailoring.individualCategories.TravelForWorkPage
import pages.expenses.travelAndAccommodation._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

/** When you change the navigator, remember to change also `next` method in each of the pages.
  */
@Singleton
class TravelAndAccommodationNavigator @Inject() {

  private val normalRoutes: Page => UserAnswers => (TaxYear, BusinessId) => Option[Call] = {

    case TravelAndAccommodationExpenseTypePage =>
      userAnswers => (taxYear, businessId) => handleTravelAndAccomodationExpenses(userAnswers, taxYear, businessId)

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
      _ => (taxYear, businessId) => Some(routes.TravelAndAccommodationExpensesCYAController.onPageLoad(taxYear, businessId))

    case TravelForWorkYourMileagePage =>
      _ =>
        (taxYear, businessId) =>
          Some(
            routes.YourFlatRateForVehicleExpensesController
              .onPageLoad(taxYear, businessId, NormalMode))

    case YourFlatRateForVehicleExpensesPage =>
      ua => (taxYear, businessId) => handleYourVehicleExpensesFlatRateChoice(ua, taxYear, businessId, NormalMode)

    case PublicTransportAndAccommodationExpensesPage =>
      ua => (taxYear, businessId) => handlePublicTransportAndAccom(ua, taxYear, businessId, NormalMode)

    case DisallowableTransportAndAccommodationPage =>
      _ =>
        (taxYear, businessId) =>
          Option(
            routes.PublicTransportAndAccommodationExpensesCYAController.onPageLoad(taxYear, businessId)
          )

    case AddAnotherVehiclePage =>
      ua => (taxYear, businessId) => Some(handleAddAnotherVehicle(ua, taxYear, businessId, NormalMode))

    case CostsNotCoveredPage =>
      _ =>
        (taxYear, businessId) =>
          Some(
            routes.TravelAndAccommodationExpensesCYAController
              .onPageLoad(taxYear, businessId))

    case TravelAndAccommodationCYAPage =>
      _ => (taxYear, businessId) => Some(routes.AddAnotherVehicleController.onPageLoad(taxYear, businessId, NormalMode))

    case _ => _ => (_, _) => None
  }

  private def handlePublicTransportAndAccom(ua: UserAnswers, taxYear: TaxYear, businessId: BusinessId, mode: Mode): Option[Call] =
    ua.get(TravelForWorkPage, businessId) map {
      case TravelForWork.YesDisallowable =>
        routes.DisallowableTransportAndAccommodationController.onPageLoad(taxYear, businessId, mode)
      case _ =>
        routes.PublicTransportAndAccommodationExpensesCYAController.onPageLoad(taxYear, businessId)
    }

  private def handleTravelAndAccomodationExpenses(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId): Option[Call] =
    userAnswers.get(TravelAndAccommodationExpenseTypePage, businessId).map(_.toSeq) match {
      case Some(Seq(PublicTransportAndOtherAccommodation)) =>
        Option(
          routes.PublicTransportAndAccommodationExpensesController.onPageLoad(taxYear, businessId, NormalMode)
        )
      case _ =>
        Some(
          routes.TravelForWorkYourVehicleController
            .onPageLoad(taxYear, businessId, NormalMode)
        )
    }

  private def handleSimplifiedExpenses(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, mode: Mode): Option[Call] =
    userAnswers.get(SimplifiedExpensesPage, businessId) map {
      case true => routes.UseSimplifiedExpensesController.onPageLoad(taxYear, businessId)
      case false =>
        routes.VehicleFlatRateChoiceController.onPageLoad(taxYear, businessId, mode)
    }

  private def handleYourVehicleExpensesFlatRateChoice(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, mode: Mode): Option[Call] =
    userAnswers.get(SimplifiedExpensesPage, businessId) flatMap {
      case true => Some(routes.CostsNotCoveredController.onPageLoad(taxYear, businessId, mode))
      case false =>
        userAnswers.get(YourFlatRateForVehicleExpensesPage, businessId) map {
          case YourFlatRateForVehicleExpenses.Flatrate   => routes.CostsNotCoveredController.onPageLoad(taxYear, businessId, mode)
          case YourFlatRateForVehicleExpenses.Actualcost => routes.VehicleExpensesController.onPageLoad(taxYear, businessId, mode)
        }
    }

  private def handleFlatRateChoice(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, mode: Mode): Option[Call] =
    userAnswers.get(VehicleFlatRateChoicePage, businessId) map {
      case true =>
        routes.TravelForWorkYourMileageController.onPageLoad(taxYear, businessId, mode)
      case false =>
        routes.VehicleExpensesController.onPageLoad(taxYear, businessId, mode)
    }

  private def handleAddAnotherVehicle(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, mode: Mode): Call =
    userAnswers.get(AddAnotherVehiclePage, businessId) match {
      case Some(true) => routes.TravelForWorkYourVehicleController.onPageLoad(taxYear, businessId, mode)
      case Some(false) =>
        userAnswers.get(TravelAndAccommodationExpenseTypePage, businessId) match {
          case Some(expenseTypes) if expenseTypes.contains(TravelAndAccommodationExpenseType.PublicTransportAndOtherAccommodation) =>
            routes.PublicTransportAndAccommodationExpensesController.onPageLoad(taxYear, businessId, mode)
          case Some(expenseTypes) if !expenseTypes.contains(TravelAndAccommodationExpenseType.PublicTransportAndOtherAccommodation) =>
            // TODO false and does not PublicTransportAndOtherAccommodation == have you finished page(last page)
            routes.VehicleExpensesController.onPageLoad(taxYear, businessId, mode)
        }
      case None => controllers.standard.routes.JourneyRecoveryController.onPageLoad()
    }

  private val checkRouteMap: Page => UserAnswers => (TaxYear, BusinessId) => Option[Call] = {

    case TravelAndAccommodationExpenseTypePage =>
      _ =>
        (taxYear, businessId) =>
          Some(
            routes.TravelAndAccommodationExpensesCYAController
              .onPageLoad(taxYear, businessId))

    case TravelForWorkYourVehiclePage =>
      _ => (taxYear, businessId) => Some(routes.TravelAndAccommodationExpensesCYAController.onPageLoad(taxYear, businessId))

    case VehicleTypePage =>
      _ => (taxYear, businessId) => Some(routes.TravelAndAccommodationExpensesCYAController.onPageLoad(taxYear, businessId))

    case SimplifiedExpensesPage =>
      ua => (taxYear, businessId) => handleSimplifiedExpenses(ua, taxYear, businessId, NormalMode)

    case VehicleFlatRateChoicePage =>
      ua => (taxYear, businessId) => handleFlatRateChoice(ua, taxYear, businessId, NormalMode)

    case TravelForWorkYourMileagePage =>
      _ =>
        (taxYear, businessId) =>
          Some(
            routes.YourFlatRateForVehicleExpensesController
              .onPageLoad(taxYear, businessId, NormalMode))

    case YourFlatRateForVehicleExpensesPage =>
      ua => (taxYear, businessId) => handleYourVehicleExpensesFlatRateChoice(ua, taxYear, businessId, NormalMode)

    case CostsNotCoveredPage =>
      _ =>
        (taxYear, businessId) =>
          Some(
            routes.TravelAndAccommodationExpensesCYAController
              .onPageLoad(taxYear, businessId))

    case RemoveVehiclePage =>
      ua => (taxYear, businessId) => handleRemoveVehicle(ua, taxYear, businessId)

    case AddAnotherVehiclePage =>
      ua => (taxYear, businessId) => Some(handleAddAnotherVehicle(ua, taxYear, businessId, NormalMode))

    case _ =>
      _ => (_, _) => Some(controllers.standard.routes.JourneyRecoveryController.onPageLoad())
  }

  private def handleRemoveVehicle(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId): Option[Call] =
    userAnswers.get(RemoveVehiclePage, businessId) map {
      case true => routes.AddAnotherVehicleController.onPageLoad(taxYear, businessId, CheckMode)
      case false =>
        routes.AddAnotherVehicleController.onPageLoad(taxYear, businessId, NormalMode)
    }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId): Call =
    mode match {
      case NormalMode =>
        normalRoutes(page)(userAnswers)(taxYear, businessId).getOrElse(controllers.standard.routes.JourneyRecoveryController.onPageLoad())
      case CheckMode =>
        checkRouteMap(page)(userAnswers)(taxYear, businessId).getOrElse(controllers.standard.routes.JourneyRecoveryController.onPageLoad())
    }
}
