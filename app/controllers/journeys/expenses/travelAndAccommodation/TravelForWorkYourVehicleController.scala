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
import forms.expenses.travelAndAccommodation.TravelForWorkYourVehicleFormProvider
import models.Mode
import models.common.Journey.ExpensesVehicleDetails
import models.common.{BusinessId, TaxYear}
import models.journeys.expenses.travelAndAccommodation.VehicleDetailsDb
import navigation.TravelAndAccommodationNavigator
import pages.expenses.travelAndAccommodation.TravelForWorkYourVehiclePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.answers.AnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.travelAndAccommodation.TravelForWorkYourVehicleView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TravelForWorkYourVehicleController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: TravelAndAccommodationNavigator,
    formProvider: TravelForWorkYourVehicleFormProvider,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    val controllerComponents: MessagesControllerComponents,
    answersService: AnswersService,
    view: TravelForWorkYourVehicleView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val page = TravelForWorkYourVehiclePage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val ctx                = request.mkJourneyNinoContext(taxYear, businessId, ExpensesVehicleDetails)
      val form: Form[String] = formProvider(request.userType)

      answersService.getAnswers[VehicleDetailsDb](ctx, Some(1)).map { optVehicleDetails =>
        val preparedForm = optVehicleDetails
          .flatMap(_.description)
          .fold(form)(form.fill)

        Ok(view(preparedForm, mode, request.userType, taxYear, businessId))
      }
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val ctx = request.mkJourneyNinoContext(taxYear, businessId, ExpensesVehicleDetails)

      formProvider(request.userType)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(page, value, Some(businessId)))
              _              <- sessionRepository.set(updatedAnswers)
              oldAnswers     <- answersService.getAnswers[VehicleDetailsDb](ctx, Some(1))
              _ <- answersService.replaceAnswers(
                ctx = ctx,
                data = oldAnswers
                  .getOrElse(VehicleDetailsDb())
                  .copy(description = Some(value)),
                index = Some(1)
              )
            } yield Redirect(navigator.nextPage(page, mode, updatedAnswers, taxYear, businessId))
        )
    }
}
