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

import controllers.journeys
import controllers.journeys.expenses.travelAndAccommodation.routes
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.TravelForWork
import models.journeys.expenses.travelAndAccommodation.TravelAndAccommodationExpenseType.PublicTransportAndOtherAccommodation
import models.journeys.expenses.travelAndAccommodation.{
  TravelAndAccommodationExpenseType,
  TravelExpensesDb,
  VehicleDetailsDb,
  YourFlatRateForVehicleExpenses
}
import models.{NormalMode, _}
import pages._
import pages.expenses.TravelAndAccommodationDisallowableExpensesPage
import pages.expenses.tailoring.individualCategories.TravelForWorkPage
import pages.expenses.travelAndAccommodation._
import pages.travelAndAccommodation.TravelAndAccommodationTotalExpensesPage
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

/** When you change the navigator, remember to change also `next` method in each of the pages.
  */
@Singleton
class TravelAndAccommodationNavigator @Inject() {

  private val normalRoutes: Page => UserAnswers => (TaxYear, BusinessId) => Option[Call] = {

    case TravelAndAccommodationExpenseTypePage =>
      userAnswers => (taxYear, businessId) => handleTravelAndAccomodationExpenses(userAnswers, taxYear, businessId)

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
            .onPageLoad(taxYear, businessId, Index(1), NormalMode)
        )
    }

  private def handleSimplifiedExpenses(data: VehicleDetailsDb, taxYear: TaxYear, businessId: BusinessId, index: Index, mode: Mode): Option[Call] =
    data.usedSimplifiedExpenses map {
      case true => routes.UseSimplifiedExpensesController.onPageLoad(taxYear, businessId, index)
      case false =>
        routes.VehicleFlatRateChoiceController.onPageLoad(taxYear, businessId, index, mode)
    }

  private def handleYourVehicleExpensesFlatRateChoice(vehicleDetails: VehicleDetailsDb,
                                                      taxYear: TaxYear,
                                                      businessId: BusinessId,
                                                      index: Index,
                                                      mode: Mode): Option[Call] =
    vehicleDetails.usedSimplifiedExpenses flatMap {
      case true => Some(routes.CostsNotCoveredController.onPageLoad(taxYear, businessId, mode))
      case false =>
        vehicleDetails.expenseMethod map {
          case YourFlatRateForVehicleExpenses.Flatrate   => routes.CostsNotCoveredController.onPageLoad(taxYear, businessId, mode)
          case YourFlatRateForVehicleExpenses.Actualcost => routes.VehicleExpensesController.onPageLoad(taxYear, businessId, index, mode)
        }
    }

  private def handleFlatRateChoice(vehicleDetails: VehicleDetailsDb,
                                   taxYear: TaxYear,
                                   businessId: BusinessId,
                                   index: Index,
                                   mode: Mode): Option[Call] =
    vehicleDetails.calculateFlatRate map {
      case true =>
        routes.TravelForWorkYourMileageController.onPageLoad(taxYear, businessId, index, mode)
      case false =>
        routes.VehicleExpensesController.onPageLoad(taxYear, businessId, index, mode)
    }

  private def handleAddAnotherVehicle(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, mode: Mode): Call =
    userAnswers.get(AddAnotherVehiclePage, businessId) match {
      case Some(true) => routes.TravelForWorkYourVehicleController.onPageLoad(taxYear, businessId, Index(1), mode)
      case Some(false) =>
        userAnswers.get(TravelAndAccommodationExpenseTypePage, businessId) match {
          case Some(expenseTypes) if expenseTypes.contains(TravelAndAccommodationExpenseType.PublicTransportAndOtherAccommodation) =>
            routes.PublicTransportAndAccommodationExpensesController.onPageLoad(taxYear, businessId, mode)
          case Some(expenseTypes) if !expenseTypes.contains(TravelAndAccommodationExpenseType.PublicTransportAndOtherAccommodation) =>
            // TODO false and does not PublicTransportAndOtherAccommodation == have you finished page(last page)
            routes.VehicleExpensesController.onPageLoad(taxYear, businessId, Index(1), mode) // TODO handle index
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

  private def handleTravelAndAccom(ua: UserAnswers, taxYear: TaxYear, businessId: BusinessId, mode: Mode): Option[Call] =
    ua.get(TravelForWorkPage, businessId) map {
      case TravelForWork.YesDisallowable =>
        routes.TravelAndAccommodationDisallowableExpensesController.onPageLoad(taxYear, businessId, mode)
      case _ =>
        ???
    }

  private val normalTravelExpensesRoutes: Page => TravelExpensesDb => (TaxYear, BusinessId, UserAnswers) => Option[Call] = {
    case TravelAndAccommodationTotalExpensesPage =>
      _ => (taxYear, businessId, userAnswers) => handleTravelAndAccom(userAnswers, taxYear, businessId, NormalMode)
    case TravelAndAccommodationDisallowableExpensesPage =>
      _ => (taxYear, businessId, userAnswers) => Some(journeys.routes.TaskListController.onPageLoad(taxYear)) // TODO redirect to new CYA page
    case _ =>
      _ => (_, _, _) => Some(controllers.standard.routes.JourneyRecoveryController.onPageLoad())
  }

  def nextTravelExpensesPage(page: Page,
                             mode: Mode,
                             model: TravelExpensesDb,
                             taxYear: TaxYear,
                             businessId: BusinessId,
                             userAnswers: UserAnswers): Call =
    mode match {
      case NormalMode =>
        normalTravelExpensesRoutes(page)(model)(taxYear, businessId, userAnswers)
          .getOrElse(controllers.standard.routes.JourneyRecoveryController.onPageLoad())
      case CheckMode =>
        checkTravelExpensesRouteMap(page)(model)(taxYear, businessId).getOrElse(controllers.standard.routes.JourneyRecoveryController.onPageLoad())
    }

  private def normalIndexRoutes: Page => VehicleDetailsDb => (TaxYear, BusinessId, Index) => Option[Call] = {
    case TravelForWorkYourVehiclePage =>
      _ => (taxYear, businessId, index) => Some(routes.VehicleTypeController.onPageLoad(taxYear, businessId, index, NormalMode))
    case VehicleTypePage =>
      _ => (taxYear, businessId, index) => Some(routes.SimplifiedExpensesController.onPageLoad(taxYear, businessId, index, NormalMode))

    case SimplifiedExpensesPage =>
      data => (taxYear, businessId, index) => handleSimplifiedExpenses(data, taxYear, businessId, index, NormalMode)

    case VehicleFlatRateChoicePage =>
      data => (taxYear, businessId, index) => handleFlatRateChoice(data, taxYear, businessId, index, NormalMode)

    case TravelForWorkYourMileagePage =>
      _ => (taxYear, businessId, index) => Some(routes.YourFlatRateForVehicleExpensesController.onPageLoad(taxYear, businessId, NormalMode))

    case VehicleExpensesPage =>
      _ => (taxYear, businessId, index) => Some(routes.TravelAndAccommodationExpensesCYAController.onPageLoad(taxYear, businessId))

    case YourFlatRateForVehicleExpensesPage =>
      data => (taxYear, businessId, index) => handleYourVehicleExpensesFlatRateChoice(data, taxYear, businessId, index, NormalMode)
    case _ => _ => (_, _, _) => None
  }

  private def checkIndexRouteMap: Page => VehicleDetailsDb => (TaxYear, BusinessId, Index) => Option[Call] = {

    case TravelForWorkYourVehiclePage =>
      _ => (taxYear, businessId, index) => Some(routes.TravelAndAccommodationExpensesCYAController.onPageLoad(taxYear, businessId))

    case VehicleTypePage =>
      _ => (taxYear, businessId, index) => Some(routes.TravelAndAccommodationExpensesCYAController.onPageLoad(taxYear, businessId))

    case SimplifiedExpensesPage =>
      data => (taxYear, businessId, index) => handleSimplifiedExpenses(data, taxYear, businessId, index, NormalMode)

    case VehicleFlatRateChoicePage =>
      data => (taxYear, businessId, index) => handleFlatRateChoice(data, taxYear, businessId, index, NormalMode)

    case TravelForWorkYourMileagePage =>
      _ =>
        (taxYear, businessId, index) =>
          Some(
            routes.YourFlatRateForVehicleExpensesController
              .onPageLoad(taxYear, businessId, NormalMode))

    case VehicleExpensesPage =>
      _ => (taxYear, businessId, index) => Some(routes.TravelAndAccommodationExpensesCYAController.onPageLoad(taxYear, businessId))

    case YourFlatRateForVehicleExpensesPage =>
      data => (taxYear, businessId, index) => handleYourVehicleExpensesFlatRateChoice(data, taxYear, businessId, index, NormalMode)
    case _ => _ => (_, _, _) => None
  }

  def nextIndexPage(page: Page, mode: Mode, model: VehicleDetailsDb, taxYear: TaxYear, businessId: BusinessId, index: Index): Call =
    mode match {
      case NormalMode =>
        normalIndexRoutes(page)(model)(taxYear, businessId, index).getOrElse(controllers.standard.routes.JourneyRecoveryController.onPageLoad())
      case CheckMode =>
        checkIndexRouteMap(page)(model)(taxYear, businessId, index).getOrElse(controllers.standard.routes.JourneyRecoveryController.onPageLoad())
    }

  private val checkTravelExpensesRouteMap: Page => TravelExpensesDb => (TaxYear, BusinessId) => Option[Call] = {

    case TravelAndAccommodationTotalExpensesPage =>
      _ =>
        (taxYear, businessId) =>
          Some(
            routes.TravelAndAccommodationExpensesCYAController
              .onPageLoad(taxYear, businessId))
  }
}
