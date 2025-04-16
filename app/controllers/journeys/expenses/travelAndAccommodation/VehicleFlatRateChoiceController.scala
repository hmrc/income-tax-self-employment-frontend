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
import forms.expenses.travelAndAccommodation.VehicleFlatRateChoiceFormProvider
import models.common.Journey.ExpensesVehicleDetails
import models.common.{BusinessId, TaxYear}
import models.journeys.expenses.travelAndAccommodation.VehicleDetailsDb
import models.{Index, Mode}
import navigation.TravelAndAccommodationNavigator
import pages.expenses.travelAndAccommodation.VehicleFlatRateChoicePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.answers.AnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.travelAndAccommodation.VehicleFlatRateChoiceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VehicleFlatRateChoiceController @Inject() (
    override val messagesApi: MessagesApi,
    navigator: TravelAndAccommodationNavigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: VehicleFlatRateChoiceFormProvider,
    answersService: AnswersService,
    val controllerComponents: MessagesControllerComponents,
    view: VehicleFlatRateChoiceView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val page = VehicleFlatRateChoicePage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val ctx = request.mkJourneyNinoContext(taxYear, businessId, ExpensesVehicleDetails)
      answersService.getAnswers[VehicleDetailsDb](ctx, Some(index)).map { optVehicleDetails =>
        getVehicleNameAndLoadPage(optVehicleDetails) { name =>
          val form: Form[Boolean] = formProvider(name, request.userType)
          val preparedForm = optVehicleDetails
            .flatMap(_.calculateFlatRate)
            .fold(form)(form.fill)
          Ok(view(preparedForm, name, request.userType, taxYear, businessId, index, mode))
        }
      }
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val ctx = request.mkJourneyNinoContext(taxYear, businessId, ExpensesVehicleDetails)
      answersService.getAnswers[VehicleDetailsDb](ctx, Some(index)).flatMap { optVehicleDetails =>
        optVehicleDetails.flatMap(_.description) match {
          case Some(vehicleName) =>
            val form: Form[Boolean] = formProvider(vehicleName, request.userType)
            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(BadRequest(view(formWithErrors, vehicleName, request.userType, taxYear, businessId, index, mode))),
                value =>
                  for {
                    oldAnswers <- answersService.getAnswers[VehicleDetailsDb](ctx, Some(index))
                    newData <- answersService.replaceAnswers(
                      ctx = ctx,
                      data = updateData(value,
                        oldAnswers
                          .getOrElse(VehicleDetailsDb())
                          .copy(calculateFlatRate = Some(value))),
                        Some(index)
                    )
                  } yield Redirect(navigator.nextIndexPage(page, mode, newData, taxYear, businessId, index))
              )
          case None =>
            Future.successful(Redirect(controllers.standard.routes.JourneyRecoveryController.onPageLoad()))
        }
      }
    }

  private def updateData(flatRateChoice: Boolean, vehicleDetails: VehicleDetailsDb): VehicleDetailsDb =
    if (!flatRateChoice) {
      vehicleDetails.copy(workMileage = None, costsOutsideFlatRate = None, expenseMethod = None)
    } else {
      vehicleDetails
    }
}
