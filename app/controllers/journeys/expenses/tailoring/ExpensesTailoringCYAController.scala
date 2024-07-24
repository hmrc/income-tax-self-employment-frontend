/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.journeys.expenses.tailoring

import controllers.actions._
import controllers.handleSubmitAnswersResult
import controllers.journeys.expenses.tailoring
import models.CheckMode
import models.common._
import models.journeys.Journey
import models.journeys.Journey.ExpensesTailoring
import models.journeys.expenses.ExpensesTailoring.{IndividualCategories, NoExpenses, TotalAmount}
import models.journeys.expenses.ExpensesTailoringAnswers
import models.journeys.expenses.ExpensesTailoringAnswers._
import pages.expenses.tailoring.{ExpensesCategoriesPage, ExpensesTailoringCYAPage}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.expenses.tailoring._
import views.html.standard.CheckYourAnswersView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ExpensesTailoringCYAController @Inject() (override val messagesApi: MessagesApi,
                                                identify: IdentifierAction,
                                                getUserAnswers: DataRetrievalAction,
                                                getJourneyAnswersIfAny: SubmittedDataRetrievalActionProvider,
                                                requireData: DataRequiredAction,
                                                hopChecker: HopCheckerAction,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: CheckYourAnswersView,
                                                service: SelfEmploymentService)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  private implicit val logger: Logger = Logger(this.getClass)
  private val page                    = ExpensesTailoringCYAPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] =
    (identify andThen getUserAnswers andThen
      getJourneyAnswersIfAny[ExpensesTailoringAnswers](request =>
        request.mkJourneyNinoContext(taxYear, businessId, Journey.ExpensesTailoring)) andThen
      requireData andThen
      hopChecker.hasPreviousAnswers(Journey.ExpensesTailoring, page, taxYear, businessId, CheckMode)) { implicit request =>
      val summaryList = buildTailoringSummaryList(request.userAnswers, taxYear, businessId, request.userType)
      (request.valueOrRedirectDefault(ExpensesCategoriesPage, businessId) map { answer =>
        val title = s"${ExpensesTailoringCYAPage.toString}${if (answer == IndividualCategories) "Categories" else ""}"
        Ok(
          view(
            title,
            taxYear,
            request.userType,
            summaryList,
            tailoring.routes.ExpensesTailoringCYAController.onSubmit(taxYear, businessId)
          )
        )
      }).merge
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getUserAnswers andThen requireData) async {
    implicit request =>
      request.valueOrRedirectDefault(ExpensesCategoriesPage, businessId) match {
        case Left(redirect) => Future(redirect)
        case Right(answer) =>
          val (journeyContext, result) = answer match {
            case TotalAmount =>
              val journeyContext = JourneyContextWithNino(taxYear, request.nino, businessId, request.mtditid, ExpensesTailoring, Some("total"))
              (journeyContext, service.submitAnswers[AsOneTotalAnswers](journeyContext, request.userAnswers))
            case IndividualCategories =>
              val journeyContext =
                JourneyAnswersContext(taxYear, request.nino, businessId, request.mtditid, ExpensesTailoring, Some("categories"))
              (journeyContext, service.submitAnswers[ExpensesTailoringIndividualCategoriesAnswers](journeyContext, request.userAnswers))
            case NoExpenses =>
              val journeyContext =
                JourneyAnswersContext(taxYear, request.nino, businessId, request.mtditid, ExpensesTailoring, Some("none"))
              (journeyContext, service.submitAnswers[NoExpensesAnswers.type](journeyContext, request.userAnswers))
          }

          handleSubmitAnswersResult(journeyContext, result)
      }
  }
}
