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
import controllers.journeys.fillForm
import forms.expenses.travelAndAccommodation.DisallowableTransportAndAccommodationFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import navigation.TravelAndAccommodationNavigator
import pages.{CostsNotCoveredPage, DisallowableTransportAndAccommodationPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.MoneyUtils.formatMoney
import views.html.journeys.expenses.travelAndAccommodation.DisallowableTransportAndAccommodationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DisallowableTransportAndAccommodationController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: TravelAndAccommodationNavigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: DisallowableTransportAndAccommodationFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: DisallowableTransportAndAccommodationView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val page = DisallowableTransportAndAccommodationPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      request.userAnswers.get(CostsNotCoveredPage, businessId) match {
        case Some(expenses) =>
          val form       = formProvider(request.userType, expenses)
          val filledForm = fillForm(page, businessId, form)
          Ok(view(filledForm, mode, request.userType, taxYear, businessId, formatMoney(expenses)))
        case _ => Redirect(controllers.standard.routes.JourneyRecoveryController.onPageLoad())
      }

  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(CostsNotCoveredPage, businessId) match {
        case Some(expenses) =>
          val form = formProvider(request.userType, expenses)
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, formatMoney(expenses)))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(DisallowableTransportAndAccommodationPage, value, Some(businessId)))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(DisallowableTransportAndAccommodationPage, mode, updatedAnswers, taxYear, businessId))
            )
        case _ => Future.successful(Redirect(controllers.standard.routes.JourneyRecoveryController.onPageLoad()))
      }
  }
}
