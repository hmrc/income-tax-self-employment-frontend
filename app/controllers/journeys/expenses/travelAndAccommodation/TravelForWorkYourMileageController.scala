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
import forms.expenses.travelAndAccommodation.TravelForWorkYourMileageFormProvider
import models.common.Journey.ExpensesVehicleDetails
import models.common.{BusinessId, TaxYear}
import models.journeys.expenses.travelAndAccommodation.VehicleDetailsDb
import models.{Index, Mode}
import navigation.TravelAndAccommodationNavigator
import pages.expenses.travelAndAccommodation.TravelForWorkYourMileagePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.answers.AnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.travelAndAccommodation.TravelForWorkYourMileageView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TravelForWorkYourMileageController @Inject() (
    override val messagesApi: MessagesApi,
    navigator: TravelAndAccommodationNavigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: TravelForWorkYourMileageFormProvider,
    val controllerComponents: MessagesControllerComponents,
    answersService: AnswersService,
    view: TravelForWorkYourMileageView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val page = TravelForWorkYourMileagePage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val ctx = request.mkJourneyNinoContext(taxYear, businessId, ExpensesVehicleDetails)

      answersService.getAnswers[VehicleDetailsDb](ctx, Some(index)).map { optVehicleDetails =>
        getVehicleNameAndLoadPage(optVehicleDetails) { name =>
          val form: Form[BigDecimal] = formProvider(request.userType, name)
          val preparedForm = optVehicleDetails
            .flatMap(_.workMileage)
            .fold(form)(form.fill)
          Ok(view(preparedForm, mode, request.userType, taxYear, businessId, name, index))
        }
      }
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val ctx = request.mkJourneyNinoContext(taxYear, businessId, ExpensesVehicleDetails)

      answersService.getAnswers[VehicleDetailsDb](ctx, Some(index)).flatMap { optVehicleDetails =>
        optVehicleDetails.flatMap(_.description) match {
          case Some(vehicle) =>
            val form: Form[BigDecimal] = formProvider(request.userType, vehicle)
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, vehicle, index))),
                value =>
                  for {
                    oldAnswers <- answersService.getAnswers[VehicleDetailsDb](ctx, Some(index))
                    newData <- answersService.replaceAnswers(
                      ctx = ctx,
                      data = oldAnswers
                        .getOrElse(VehicleDetailsDb())
                        .copy(workMileage = Some(value)),
                      Some(index)
                    )
                  } yield Redirect(navigator.nextIndexPage(page, mode, newData, taxYear, businessId, index))
              )
          case None =>
            Future.successful(Redirect(controllers.standard.routes.JourneyRecoveryController.onPageLoad()))
        }
      }
    }
}
