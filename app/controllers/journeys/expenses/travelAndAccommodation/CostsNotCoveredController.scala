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
import forms.standard.CurrencyFormProvider
import models.common.Journey.ExpensesVehicleDetails
import models.common.{BusinessId, TaxYear, UserType}
import models.journeys.expenses.travelAndAccommodation.VehicleDetailsDb
import models.{Index, Mode}
import navigation.TravelAndAccommodationNavigator
import pages.CostsNotCoveredPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.answers.AnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.travelAndAccommodation.CostsNotCoveredView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CostsNotCoveredController @Inject() (
    override val messagesApi: MessagesApi,
    navigator: TravelAndAccommodationNavigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: CurrencyFormProvider,
    val controllerComponents: MessagesControllerComponents,
    answersService: AnswersService,
    view: CostsNotCoveredView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = (userType: UserType) =>
    formProvider(
      page = CostsNotCoveredPage,
      userType = userType,
      minValueError = s"costsNotCovered.error.lessThanZero.$userType",
      maxValueError = s"costsNotCovered.error.overMax.$userType",
      nonNumericError = s"costsNotCovered.error.nonNumeric.$userType"
    )

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val ctx = request.mkJourneyNinoContext(taxYear, businessId, ExpensesVehicleDetails)
      answersService
        .getAnswers[VehicleDetailsDb](ctx, Some(index))
        .map {
          case Some(VehicleDetailsDb(_, _, _, _, _, _, Some(costsOutsideFlatRate), _)) =>
            form(request.userType).fill(costsOutsideFlatRate)
          case _ =>
            form(request.userType)
        }
        .map { preparedForm =>
          Ok(view(preparedForm, mode, request.userType, taxYear, businessId, index))
        }
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val ctx = request.mkJourneyNinoContext(taxYear, businessId, ExpensesVehicleDetails)
      form(request.userType)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, index))),
          value =>
            answersService.getAnswers[VehicleDetailsDb](ctx, Some(index)).flatMap {
              case Some(preValue) =>
                for {
                  newData <- answersService.replaceAnswers(
                    ctx,
                    data = preValue.copy(costsOutsideFlatRate = Option(value)),
                    Some(index)
                  )
                } yield Redirect(navigator.nextIndexPage(CostsNotCoveredPage, mode, newData, taxYear, businessId, index))
              case _ =>
                Future.successful(Redirect(controllers.standard.routes.JourneyRecoveryController.onPageLoad()))
            }
        )
    }
}
