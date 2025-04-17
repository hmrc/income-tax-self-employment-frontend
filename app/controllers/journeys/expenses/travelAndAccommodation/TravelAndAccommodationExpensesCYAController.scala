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

package controllers.journeys.expenses.travelAndAccommodation

import controllers.actions._
import models.{Index, NormalMode}
import models.common.Journey.{ExpensesTravelForWork, ExpensesVehicleDetails}
import models.common.{BusinessId, TaxYear}
import models.journeys.expenses.travelAndAccommodation.YourFlatRateForVehicleExpenses.{Actualcost, Flatrate}
import models.journeys.expenses.travelAndAccommodation.{TravelAndAccommodationJourneyAnswers, VehicleDetailsDb}
import navigation.TravelAndAccommodationNavigator
import pages.expenses.travelAndAccommodation.TravelAndAccommodationCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.answers.AnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.checkAnswers.expenses.travelAndAccommodation._
import viewmodels.journeys.SummaryListCYA
import views.html.standard.CheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TravelAndAccommodationExpensesCYAController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    getJourneyAnswers: SubmittedDataRetrievalActionProvider,
    requireData: DataRequiredAction,
    navigator: TravelAndAccommodationNavigator,
    val controllerComponents: MessagesControllerComponents,
    answersService: AnswersService,
    view: CheckYourAnswersView
)(implicit executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, index: Index): Action[AnyContent] =
    (identify andThen getData andThen getJourneyAnswers[TravelAndAccommodationJourneyAnswers](req =>
      req.mkJourneyNinoContext(taxYear, businessId, ExpensesTravelForWork)) andThen requireData).async { implicit request =>
      val ctx = request.mkJourneyNinoContext(taxYear, businessId, ExpensesVehicleDetails)
      answersService
        .getAnswers[VehicleDetailsDb](ctx, Option(index))
        .map {
          case Some(
                VehicleDetailsDb(
                  Some(vehicleName),
                  Some(vehicleType),
                  Some(false), // usedSimplifiedExpenses
                  Some(true),  // calculateFlatRate
                  Some(workMileage),
                  Some(Flatrate), // expenseMethod FlatRate, or ActualCosts
                  Some(costsOutsideFlatRate),
                  _
                )
              ) =>
            val summaryList = SummaryListCYA.summaryListOpt(
              rows = List(
                VehicleExpenseTypeSummary.row(request.userAnswers, taxYear, businessId, request.userType),
                VehicleNameSummary.row(vehicleName, taxYear, businessId, request.userType, index),
                VehicleTypeSummary.row(vehicleName, vehicleType, taxYear, businessId, index),
                SimplifiedExpensesSummary.row(vehicleName, false, taxYear, businessId, request.userType, index),
                VehicleFlatRateChoiceSummary.row(false, true, taxYear, businessId, request.userType, index),
                TravelForWorkYourMileageSummary.row(vehicleName, workMileage, taxYear, businessId, request.userType, index),
                YourFlatRateForVehicleExpensesSummary.row(workMileage, Flatrate, taxYear, businessId, request.userType),
                CostsNotCoveredSummary.row(costsOutsideFlatRate, taxYear, businessId, request.userType)
                // VehicleExpensesSummary.row(vehicleExpenses, taxYear, businessId, request.userType, index)
              )
            )
            Ok(
              view(
                TravelAndAccommodationCYAPage,
                taxYear,
                request.userType,
                summaryList,
                routes.TravelAndAccommodationExpensesCYAController.onSubmit(taxYear, businessId, index)
              )
            )
          case Some(
                VehicleDetailsDb(
                  Some(vehicleName),
                  Some(vehicleType),
                  Some(false), // usedSimplifiedExpenses
                  Some(true),  // calculateFlatRate
                  Some(workMileage),
                  Some(Actualcost), // expenseMethod FlatRate, or ActualCosts
                  _,
                  Some(vehicleExpenses)
                )
              ) =>
            val summaryList = SummaryListCYA.summaryListOpt(
              rows = List(
                VehicleExpenseTypeSummary.row(request.userAnswers, taxYear, businessId, request.userType),
                VehicleNameSummary.row(vehicleName, taxYear, businessId, request.userType, index),
                VehicleTypeSummary.row(vehicleName, vehicleType, taxYear, businessId, index),
                SimplifiedExpensesSummary.row(vehicleName, false, taxYear, businessId, request.userType, index),
                VehicleFlatRateChoiceSummary.row(false, true, taxYear, businessId, request.userType, index),
                TravelForWorkYourMileageSummary.row(vehicleName, workMileage, taxYear, businessId, request.userType, index),
                YourFlatRateForVehicleExpensesSummary.row(workMileage, Actualcost, taxYear, businessId, request.userType),
//                CostsNotCoveredSummary.row(costsOutsideFlatRate, taxYear, businessId, request.userType),
                VehicleExpensesSummary.row(vehicleExpenses, taxYear, businessId, request.userType, index)
              )
            )
            Ok(
              view(
                TravelAndAccommodationCYAPage,
                taxYear,
                request.userType,
                summaryList,
                routes.TravelAndAccommodationExpensesCYAController.onSubmit(taxYear, businessId, index)
              )
            )
          case Some(
                VehicleDetailsDb(
                  Some(vehicleName),
                  Some(vehicleType),
                  Some(false), // usedSimplifiedExpenses
                  Some(false), // calculateFlatRate
                  None,        // workMileage
                  None,        // expenseMethod
                  None,        // costsOutsideFlatRate
                  Some(vehicleExpenses)
                )
              ) =>
            val summaryList = SummaryListCYA.summaryListOpt(
              rows = List(
                VehicleExpenseTypeSummary.row(request.userAnswers, taxYear, businessId, request.userType),
                VehicleNameSummary.row(vehicleName, taxYear, businessId, request.userType, index),
                VehicleTypeSummary.row(vehicleName, vehicleType, taxYear, businessId, index),
                SimplifiedExpensesSummary.row(vehicleName, false, taxYear, businessId, request.userType, index),
                VehicleFlatRateChoiceSummary.row(false, false, taxYear, businessId, request.userType, index),
//                TravelForWorkYourMileageSummary.row(vehicleName, workMileage, taxYear, businessId, request.userType, index),
//                YourFlatRateForVehicleExpensesSummary.row(workMileage, expenseMethod, taxYear, businessId, request.userType),
//                CostsNotCoveredSummary.row(costsOutsideFlatRate, taxYear, businessId, request.userType),
                VehicleExpensesSummary.row(vehicleExpenses, taxYear, businessId, request.userType, index)
              )
            )
            Ok(
              view(
                TravelAndAccommodationCYAPage,
                taxYear,
                request.userType,
                summaryList,
                routes.TravelAndAccommodationExpensesCYAController.onSubmit(taxYear, businessId, index)
              )
            )
          case Some(
                VehicleDetailsDb(
                  Some(vehicleName),
                  Some(vehicleType),
                  Some(true), // usedSimplifiedExpenses
                  None,
                  Some(workMileage),
                  None, // FlatRate, or ActualCosts
                  Some(costsOutsideFlatRate),
                  None // vehicleExpenses
                )
              ) =>
            val summaryList = SummaryListCYA.summaryListOpt(
              rows = List(
                VehicleExpenseTypeSummary.row(request.userAnswers, taxYear, businessId, request.userType),
                VehicleNameSummary.row(vehicleName, taxYear, businessId, request.userType, index),
                VehicleTypeSummary.row(vehicleName, vehicleType, taxYear, businessId, index),
                SimplifiedExpensesSummary.row(vehicleName, true, taxYear, businessId, request.userType, index),
//                VehicleFlatRateChoiceSummary.row(usedSimplifiedExpenses, calculateFlatRate, taxYear, businessId, request.userType, index),
                TravelForWorkYourMileageSummary.row(vehicleName, workMileage, taxYear, businessId, request.userType, index),
//                YourFlatRateForVehicleExpensesSummary.row(workMileage, expenseMethod, taxYear, businessId, request.userType),
                CostsNotCoveredSummary.row(costsOutsideFlatRate, taxYear, businessId, request.userType)
//                VehicleExpensesSummary.row(vehicleExpenses, taxYear, businessId, request.userType, index)
              )
            )
            Ok(
              view(
                TravelAndAccommodationCYAPage,
                taxYear,
                request.userType,
                summaryList,
                routes.TravelAndAccommodationExpensesCYAController.onSubmit(taxYear, businessId, index)
              )
            )
          case _ =>
            Redirect(controllers.standard.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, index: Index): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      Future.successful(
        Redirect(
          navigator.nextPage(
            TravelAndAccommodationCYAPage,
            NormalMode,
            request.userAnswers,
            taxYear,
            businessId
          ))
      )
    }
}
