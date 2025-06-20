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
import models.common.Journey.ExpensesTravelForWork
import models.common._
import models.journeys.expenses.travelAndAccommodation.TravelExpensesDb
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import services.answers.AnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.checkAnswers.expenses.travelAndAccommodation.TravelAndAccommodationDisallowableExpensesSummary
import views.html.standard.CheckYourAnswersView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TravelAndAccommodationDisallowableExpensesCYAController @Inject() (override val messagesApi: MessagesApi,
                                                                         identify: IdentifierAction,
                                                                         answersService: AnswersService,
                                                                         getUserAnswers: DataRetrievalAction,
                                                                         getData: DataRetrievalAction,
                                                                         requireData: DataRequiredAction,
                                                                         service: SelfEmploymentService,
                                                                         val controllerComponents: MessagesControllerComponents,
                                                                         view: CheckYourAnswersView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val ctx = request.mkJourneyNinoContext(taxYear, businessId, ExpensesTravelForWork)
      answersService.getAnswers[TravelExpensesDb](ctx).map { optTravelExpensesData: Option[TravelExpensesDb] =>
        Ok(
          view(
            "common.checkYourAnswers",
            taxYear,
            request.userType,
            TravelAndAccommodationDisallowableExpensesSummary(optTravelExpensesData, request.userAnswers, taxYear, businessId, request.userType),
            routes.TravelAndAccommodationDisallowableExpensesCYAController.onSubmit(taxYear, businessId)
          )
        )
      }
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getUserAnswers andThen requireData) async {
    implicit request =>
      for {
        context <- Future.successful(JourneyContextWithNino(taxYear, request.nino, businessId, request.mtditid, ExpensesTravelForWork))
        ans     <- answersService.getAnswers[TravelExpensesDb](context).map(_.getOrElse(TravelExpensesDb()))
        result = service.updateTravelExpenses(taxYear, businessId, request.nino, request.mtditid, ans)
        res <- handleSubmitAnswersResult(context, result)
      } yield res
  }

}
