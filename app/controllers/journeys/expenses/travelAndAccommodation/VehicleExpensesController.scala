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
import models.Mode
import models.common.{BusinessId, TaxYear}
import models.journeys.expenses.travelAndAccommodation.TravelAndAccommodationExpenseType
import models.journeys.expenses.travelAndAccommodation.TravelAndAccommodationExpenseType.{LeasedVehicles, MyOwnVehicle}
import navigation.TravelAndAccommodationNavigator
import pages.VehicleExpensesControllerPage
import pages.expenses.travelAndAccommodation.TravelAndAccommodationExpenseTypePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.travelAndAccommodation.VehicleExpensesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VehicleExpensesController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
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

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val expenseTypes: Set[TravelAndAccommodationExpenseType] =
        request.userAnswers.get(TravelAndAccommodationExpenseTypePage).getOrElse(Set.empty[TravelAndAccommodationExpenseType])
      val form: Form[BigDecimal] = formProvider(request.user.userType)
      val preparedForm = request.userAnswers.get(VehicleExpensesControllerPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, mode, request.userType, taxYear, businessId, expenseTypes))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(TravelAndAccommodationExpenseTypePage) match {
        case Some(expenseTypes) if expenseTypes.contains(LeasedVehicles) || expenseTypes.contains(MyOwnVehicle) =>
          val form: Form[BigDecimal] = formProvider(request.user.userType)
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, expenseTypes))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(VehicleExpensesControllerPage, value, Some(businessId)))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(VehicleExpensesControllerPage, mode, updatedAnswers, taxYear, businessId))
            )
        case _ =>
          Future.successful(Redirect(controllers.standard.routes.JourneyRecoveryController.onPageLoad()))
      }
  }
}
