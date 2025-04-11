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
import controllers.journeys.{clearDependentPages, fillForm}
import forms.expenses.travelAndAccommodation.SimplifiedExpenseFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import navigation.TravelAndAccommodationNavigator
import pages.expenses.travelAndAccommodation.{SimplifiedExpensesPage, TravelForWorkYourVehiclePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.travelAndAccommodation.SimplifiedExpensesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SimplifiedExpensesController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: TravelAndAccommodationNavigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: SimplifiedExpenseFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: SimplifiedExpensesView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val page = SimplifiedExpensesPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      getVehicleNameAndLoadPage(businessId) { name =>
        val form: Form[Boolean] = formProvider(request.userType, name)
        val preparedForm        = fillForm(page, businessId, form)

        Ok(view(preparedForm, request.userType, taxYear, businessId, mode, name))
      }
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(TravelForWorkYourVehiclePage, businessId) match {
        case Some(vehicle) =>
          val form: Form[Boolean] = formProvider(request.userType, vehicle)
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userType, taxYear, businessId, mode, vehicle))),
              value =>
                for {
                  clearedAnswers <- clearDependentPages(page, value, request.userAnswers, businessId)
                  updatedAnswers <- Future.fromTry(clearedAnswers.set(page, value, Some(businessId)))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(page, mode, updatedAnswers, taxYear, businessId))
            )
        case None =>
          Future.successful(Redirect(controllers.standard.routes.JourneyRecoveryController.onPageLoad()))
      }
  }

}
