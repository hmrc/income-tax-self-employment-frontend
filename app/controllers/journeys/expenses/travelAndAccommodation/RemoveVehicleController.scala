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
import forms.expenses.travelAndAccommodation.RemoveVehicleFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import navigation.TravelAndAccommodationNavigator
import pages.expenses.travelAndAccommodation.{RemoveVehiclePage, TravelForWorkYourVehiclePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.travelAndAccommodation.RemoveVehicleView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveVehicleController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: TravelAndAccommodationNavigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: RemoveVehicleFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: RemoveVehicleView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val page = RemoveVehiclePage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      getVehicleNameAndLoadPage(businessId) { name =>
        val preparedForm = request.userAnswers.get(page, businessId) match {
          case None        => formProvider(name)
          case Some(value) => formProvider(name).fill(value)
        }

        Ok(view(preparedForm, mode, request.userType, taxYear, businessId, name))
      }
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(TravelForWorkYourVehiclePage, businessId) match {
        case Some(vehicle) =>
          val form: Form[Boolean] = formProvider(vehicle)
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, vehicle))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.remove(TravelForWorkYourVehiclePage, Some(businessId)).flatMap { answers =>
                    answers.remove(page, Some(businessId))
                  })
                  updatedAnswers <- Future.fromTry(updatedAnswers.set(page, value, Some(businessId)))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(page, mode, updatedAnswers, taxYear, businessId))
            )
        case None =>
          Future.successful(Redirect(navigator.nextPage(page, mode, request.userAnswers, taxYear, businessId)))
      }
  }

}
