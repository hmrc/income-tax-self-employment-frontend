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
import forms.expenses.travelAndAccommodation.VehicleExpensesFormProvider
import models.common.Journey.ExpensesVehicleDetails
import models.common.{BusinessId, TaxYear}
import models.journeys.expenses.travelAndAccommodation.TravelAndAccommodationExpenseType.{LeasedVehicles, MyOwnVehicle}
import models.journeys.expenses.travelAndAccommodation.{TravelAndAccommodationExpenseType, VehicleDetailsDb}
import models.{Index, Mode}
import navigation.TravelAndAccommodationNavigator
import pages.expenses.travelAndAccommodation.{TravelAndAccommodationExpenseTypePage, VehicleExpensesPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.answers.AnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.travelAndAccommodation.VehicleExpensesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VehicleExpensesController @Inject() (
    override val messagesApi: MessagesApi,
    answersService: AnswersService,
    navigator: TravelAndAccommodationNavigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: VehicleExpensesFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: VehicleExpensesView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val ctx = request.mkJourneyNinoContext(taxYear, businessId, ExpensesVehicleDetails)

      val expenseTypes: Set[TravelAndAccommodationExpenseType] =
        request.userAnswers.get(TravelAndAccommodationExpenseTypePage, businessId).getOrElse(Set.empty[TravelAndAccommodationExpenseType])

      val form: Form[BigDecimal] = formProvider(request.userType)
      answersService.getAnswers[VehicleDetailsDb](ctx, Some(index)).map { optVehicleDetails =>
        val preparedForm = optVehicleDetails
          .flatMap(_.vehicleExpenses)
          .fold(form)(form.fill)

        Ok(view(preparedForm, mode, request.userType, taxYear, businessId, expenseTypes, index))
      }
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val ctx = request.mkJourneyNinoContext(taxYear, businessId, ExpensesVehicleDetails)

      request.userAnswers.get(TravelAndAccommodationExpenseTypePage, businessId) match {
        case Some(expenseTypes) if expenseTypes.contains(LeasedVehicles) || expenseTypes.contains(MyOwnVehicle) =>
          val form: Form[BigDecimal] = formProvider(request.userType)

          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, expenseTypes, index))),
              value =>
                for {
                  oldAnswers <- answersService.getAnswers[VehicleDetailsDb](ctx, Some(index))
                  newData <- answersService.replaceAnswers(
                    ctx = ctx,
                    data = oldAnswers
                      .getOrElse(VehicleDetailsDb())
                      .copy(vehicleExpenses = Some(value)),
                    Some(index)
                  )
                } yield Redirect(navigator.nextIndexPage(VehicleExpensesPage, mode, newData, taxYear, businessId, index))
            )
        case _ =>
          Future.successful(Redirect(controllers.standard.routes.JourneyRecoveryController.onPageLoad()))
      }
    }
}
