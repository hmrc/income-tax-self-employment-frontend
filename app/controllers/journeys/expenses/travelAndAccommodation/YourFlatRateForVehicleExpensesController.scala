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
import controllers.journeys.clearDependentPages
import forms.expenses.travelAndAccommodation.YourFlatRateForVehicleExpensesFormProvider
import models.common.Journey.ExpensesVehicleDetails
import models.common.{BusinessId, TaxYear}
import models.journeys.expenses.travelAndAccommodation.{VehicleDetailsDb, YourFlatRateForVehicleExpenses}
import models.requests.DataRequest
import models.{Index, Mode}
import navigation.TravelAndAccommodationNavigator
import pages.expenses.travelAndAccommodation.{SimplifiedExpensesPage, TravelForWorkYourMileagePage, YourFlatRateForVehicleExpensesPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.answers.AnswersService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.MoneyUtils.formatMoney
import viewmodels.checkAnswers.expenses.travelAndAccommodation.TravelMileageSummaryViewModel
import views.html.journeys.expenses.travelAndAccommodation.YourFlatRateForVehicleExpensesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class YourFlatRateForVehicleExpensesController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: TravelAndAccommodationNavigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: YourFlatRateForVehicleExpensesFormProvider,
    val controllerComponents: MessagesControllerComponents,
    answersService: AnswersService,
    view: YourFlatRateForVehicleExpensesView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val ctx = request.mkJourneyNinoContext(taxYear, businessId, ExpensesVehicleDetails)

      answersService.getAnswers[VehicleDetailsDb](ctx, Some(index)).map {
        case Some(VehicleDetailsDb(_, _, Some(usedSimplifiedExpenses), _, Some(workMileage), expensesMethod, _, _)) =>
          val form: Form[YourFlatRateForVehicleExpenses] = formProvider(workMileage, request.userType)
          val preparedForm                               = expensesMethod.fold(form)(value => form.fill(value))
          loadPage(preparedForm, workMileage, usedSimplifiedExpenses, Ok, taxYear, businessId, index, mode)
        case _ =>
          Redirect(controllers.standard.routes.JourneyRecoveryController.onPageLoad())
      }
    }

  private def loadPage(form: Form[_],
                       workMileage: BigDecimal,
                       usedSimplifiedExpenses: Boolean,
                       status: Status,
                       taxYear: TaxYear,
                       businessId: BusinessId,
                       index: Index,
                       mode: Mode)(implicit request: DataRequest[AnyContent]): Result = {
    val summaryList: SummaryList          = TravelMileageSummaryViewModel.buildSummaryList(workMileage)
    val showSelection: Boolean            = !usedSimplifiedExpenses
    val totalFlatRateExpenses: BigDecimal = TravelMileageSummaryViewModel.totalFlatRateExpense(workMileage)
    status(
      view(
        form = form,
        taxYear = taxYear,
        businessId = businessId,
        index = index,
        userType = request.userType,
        mileage = stripTrailingZeros(workMileage),
        totalFlatRateExpenses = formatMoney(totalFlatRateExpenses),
        summaryList = summaryList,
        showSelection = showSelection,
        mode = mode
      ))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val ctx = request.mkJourneyNinoContext(taxYear, businessId, ExpensesVehicleDetails)

      answersService.getAnswers[VehicleDetailsDb](ctx, Some(index)).flatMap {

        case Some(vehicle @ VehicleDetailsDb(_, _, Some(true), _, _, _, _, _)) =>
          Future.successful(Redirect(navigator.nextIndexPage(YourFlatRateForVehicleExpensesPage, mode, vehicle, taxYear, businessId, index)))
        case Some(vehicle @ VehicleDetailsDb(_, _, Some(false), _, Some(workMileage), _, _, _)) =>
          val form: Form[YourFlatRateForVehicleExpenses] = formProvider(workMileage, request.userType)
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  loadPage(formWithErrors, workMileage, usedSimplifiedExpenses = false, BadRequest, taxYear, businessId, index, mode)
                ),
              value =>
                for {
                  newData <- answersService.replaceAnswers(
                    ctx = ctx,
                    data = vehicle.copy(expenseMethod = Some(value)),
                    Some(index)
                  )
                } yield Redirect(navigator.nextIndexPage(YourFlatRateForVehicleExpensesPage, mode, newData, taxYear, businessId, index))
            )
        case _ =>
          Future.successful(Redirect(controllers.standard.routes.JourneyRecoveryController.onPageLoad()))
      }
    }
}
