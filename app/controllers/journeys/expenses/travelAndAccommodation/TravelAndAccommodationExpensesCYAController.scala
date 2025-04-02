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
import controllers.handleSubmitAnswersResult
import models.NormalMode
import models.common.Journey.ExpensesTravelForWork
import models.common.{BusinessId, JourneyContextWithNino, TaxYear}
import models.journeys.expenses.travelAndAccommodation.TravelAndAccommodationJourneyAnswers
import navigation.TravelAndAccommodationNavigator
import pages.expenses.travelAndAccommodation.TravelAndAccommodationCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.checkAnswers.expenses.travelAndAccommodation.{
  CostsNotCoveredSummary,
  SimplifiedExpensesSummary,
  TravelForWorkYourMileageSummary,
  VehicleExpenseTypeSummary,
  VehicleExpensesSummary,
  VehicleFlatRateChoiceSummary,
  VehicleNameSummary,
  VehicleTypeSummary,
  YourFlatRateForVehicleExpensesSummary
}
import viewmodels.journeys.SummaryListCYA
import views.html.standard.CheckYourAnswersView

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

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] =
    (identify andThen getData andThen getJourneyAnswers[TravelAndAccommodationJourneyAnswers](req =>
      req.mkJourneyNinoContext(taxYear, businessId, ExpensesTravelForWork)) andThen requireData) { implicit request =>
      val summaryList = SummaryListCYA.summaryListOpt(
        rows = List(
          VehicleExpenseTypeSummary.row(request.userAnswers, taxYear, businessId, request.userType),
          VehicleNameSummary.row(request.userAnswers, taxYear, businessId, request.userType),
          VehicleTypeSummary.row(request.userAnswers, taxYear, businessId),
          SimplifiedExpensesSummary.row(request.userAnswers, taxYear, businessId, request.userType),
          VehicleFlatRateChoiceSummary.row(taxYear, businessId, request.userAnswers, request.userType),
          TravelForWorkYourMileageSummary.row(taxYear, businessId, request.userAnswers, request.userType),
          YourFlatRateForVehicleExpensesSummary.row(taxYear, businessId, request.userAnswers, request.userType),
          CostsNotCoveredSummary.row(request.userAnswers, taxYear, businessId, request.userType),
          VehicleExpensesSummary.row(taxYear, businessId, request.userAnswers, request.userType)
        )
      )

      Ok(
        view(
          TravelAndAccommodationCYAPage,
          taxYear,
          request.userType,
          summaryList,
          navigator.nextPage(TravelAndAccommodationCYAPage, NormalMode, request.userAnswers, taxYear, businessId)
        ))
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val context = JourneyContextWithNino(taxYear, request.nino, businessId, request.mtditid, ExpensesTravelForWork)
      val result  = service.submitAnswers[TravelAndAccommodationJourneyAnswers](context, request.userAnswers)

      handleSubmitAnswersResult(context, result)
  }
}
