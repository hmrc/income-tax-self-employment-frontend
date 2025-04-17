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
import models.common.Journey.ExpensesVehicleDetails
import models.common.{BusinessId, TaxYear}
import models.journeys.expenses.travelAndAccommodation.VehicleDetailsDb
import models.{Index, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.answers.AnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.travelAndAccommodation.UseSimplifiedExpensesView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class UseSimplifiedExpensesController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    answersService: AnswersService,
    val controllerComponents: MessagesControllerComponents,
    view: UseSimplifiedExpensesView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, index: Index): Action[AnyContent] =
    (identify andThen getData).async { implicit request =>
      val ctx = request.mkJourneyNinoContext(taxYear, businessId, ExpensesVehicleDetails)

      answersService.getAnswers[VehicleDetailsDb](ctx, Some(index)).map {
        _.flatMap(_.description) match {
          case Some(name) =>
            val redirectRoute = routes.TravelForWorkYourMileageController
              .onPageLoad(taxYear, businessId, index, NormalMode)
              .url
            Ok(view(request.userType, name, redirectRoute))
          case None =>
            Redirect(controllers.standard.routes.JourneyRecoveryController.onPageLoad())
        }
      }
    }
}
