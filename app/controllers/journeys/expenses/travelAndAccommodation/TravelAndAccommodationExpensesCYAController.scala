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
import controllers.{handleSubmitAnswersResult, routes}
import models.common.Journey.ExpensesTravelForWork
import models.common.{BusinessId, JourneyContextWithNino, TaxYear, UserType}
import models.journeys.expenses.travelAndAccommodation.TravelAndAccommodationJourneyAnswers
import pages.expenses.travelAndAccommodation.TravelAndAccommodationCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.checkAnswers.TravelForWorkYourMileageSummary
import viewmodels.checkAnswers.expenses.travelAndAccommodation.{VehicleExpenseTypeSummary, VehicleTypeSummary}
import viewmodels.journeys.SummaryListCYA
import views.html.standard.CheckYourAnswersView
import controllers.journeys.expenses.travelAndAccommodation.routes
import models.NormalMode
import navigation.TravelAndAccommodationNavigator

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TravelAndAccommodationExpensesCYAController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    getJourneyAnswers: SubmittedDataRetrievalActionProvider,
    requireData: DataRequiredAction,
    service: SelfEmploymentService,
    navigator: TravelAndAccommodationNavigator,
    val controllerComponents: MessagesControllerComponents,
    view: CheckYourAnswersView
)(implicit executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val page = TravelAndAccommodationCYAPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] =
    (identify andThen getData andThen getJourneyAnswers[TravelAndAccommodationJourneyAnswers](req =>
      req.mkJourneyNinoContext(taxYear, businessId, ExpensesTravelForWork)) andThen requireData) { implicit request =>
      val summaryList = SummaryListCYA.summaryListOpt(
        rows = List(
          VehicleExpenseTypeSummary.row(request.userAnswers, taxYear, businessId, request.userType)
        )
      )

      Ok(
        view(
          TravelAndAccommodationCYAPage,
          taxYear,
          request.userType,
          summaryList,
          navigator.nextPage(page, NormalMode, request.userAnswers, taxYear, businessId)
        ))
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val context = JourneyContextWithNino(taxYear, request.nino, businessId, request.mtditid, ExpensesTravelForWork)
      val result  = service.submitAnswers[TravelAndAccommodationJourneyAnswers](context, request.userAnswers)

      handleSubmitAnswersResult(context, result)
  }
}
