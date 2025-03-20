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
import forms.expenses.travelAndAccommodation.SimplifiedExpenseFormProvider
import handlers.ErrorHandler
import models.Mode
import models.common.{BusinessId, TaxYear}
import models.requests.DataRequest
import pages.expenses.travelAndAccommodation.{SimplifiedExpensesPage, TravelForWorkYourVehiclePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.travelAndAccommodation.SimplifiedExpensesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SimplifiedExpensesController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    // navigator: Navigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: SimplifiedExpenseFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: SimplifiedExpensesView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form: Form[Boolean] = formProvider(request.userType)
      val preparedForm = request.userAnswers.get(SimplifiedExpensesPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.userType, taxYear, businessId, mode, request.userAnswers.get(TravelForWorkYourVehiclePage).getOrElse("")))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form: Form[Boolean] = formProvider(request.userType)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, request.userType, taxYear, businessId, mode, TravelForWorkYourVehiclePage))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(SimplifiedExpensesPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(controllers.standard.routes.JourneyRecoveryController.onPageLoad().url)
          // Redirect(navigator.nextPage(SimplifiedExpensesPage, mode, updatedAnswers))
        )
  }

}
