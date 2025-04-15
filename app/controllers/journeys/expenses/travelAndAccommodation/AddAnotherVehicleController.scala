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
import forms.expenses.travelAndAccommodation.AddAnotherVehicleFormProvider
import models.{CheckMode, Index, Mode}
import models.common.{BusinessId, TaxYear}
import models.requests.DataRequest
import navigation.TravelAndAccommodationNavigator
import pages.AddAnotherVehiclePage
import pages.expenses.travelAndAccommodation.TravelForWorkYourVehiclePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.components.{OneColumnSummaryAction, OneColumnSummaryRow}
import views.html.journeys.expenses.travelAndAccommodation.AddAnotherVehicleView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddAnotherVehicleController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: TravelAndAccommodationNavigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: AddAnotherVehicleFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: AddAnotherVehicleView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form: Form[Boolean] = formProvider(request.user.userType)

      val preparedForm = request.userAnswers.get(AddAnotherVehiclePage, businessId) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, vehicleDetails(taxYear, businessId), request.user.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form: Form[Boolean] = formProvider(request.user.userType)

      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, mode, vehicleDetails(taxYear, businessId), request.userType, taxYear, businessId))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(AddAnotherVehiclePage, value, Some(businessId)))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(AddAnotherVehiclePage, mode, updatedAnswers, taxYear, businessId))
        )
  }

  private def vehicleDetails(taxYear: TaxYear, businessId: BusinessId)(implicit request: DataRequest[AnyContent]): List[OneColumnSummaryRow] = {
    val vehicles = request.userAnswers.get(TravelForWorkYourVehiclePage, businessId).toList

    if (vehicles.size == 1) {
      vehicles.map { vehicleName =>
        OneColumnSummaryRow(
          vehicleName,
          actions = List(
            OneColumnSummaryAction("site.change", routes.TravelForWorkYourVehicleController.onPageLoad(taxYear, businessId, Index(1), CheckMode).url)
          )
        )
      }
    } else {
      vehicles.map { vehicleName =>
        OneColumnSummaryRow(
          vehicleName,
          actions = List(
            OneColumnSummaryAction("site.change", routes.TravelForWorkYourVehicleController.onPageLoad(taxYear, businessId, Index(1), CheckMode).url),
            OneColumnSummaryAction("site.remove", routes.RemoveVehicleController.onPageLoad(taxYear, businessId, CheckMode).url)
          )
        )
      }
    }
  }

}
