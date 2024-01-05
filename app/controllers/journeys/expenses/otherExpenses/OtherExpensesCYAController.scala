/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.journeys.expenses.otherExpenses

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.handleSubmitAnswersResult
import controllers.journeys.expenses.otherExpenses.routes.OtherExpensesCYAController
import models.common.{BusinessId, JourneyContextWithNino, TaxYear}
import models.journeys.Journey.ExpensesOtherExpenses
import models.journeys.expenses.otherExpenses.OtherExpensesJourneyAnswers
import pages.expenses.otherExpenses.OtherExpensesCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentServiceBase
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.checkAnswers.expenses.otherExpenses.{OtherExpensesAmountSummary, OtherExpensesDisallowableAmountSummary}
import viewmodels.journeys.SummaryListCYA.summaryListOpt
import views.html.standard.CheckYourAnswersView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class OtherExpensesCYAController @Inject() (override val messagesApi: MessagesApi,
                                            val controllerComponents: MessagesControllerComponents,
                                            identify: IdentifierAction,
                                            getAnswers: DataRetrievalAction,
                                            requireAnswers: DataRequiredAction,
                                            service: SelfEmploymentServiceBase,
                                            view: CheckYourAnswersView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getAnswers andThen requireAnswers) {
    implicit request =>
      val summaryList = summaryListOpt(
        List(
          OtherExpensesAmountSummary.row(request.userAnswers, taxYear, businessId, request.userType),
          OtherExpensesDisallowableAmountSummary.row(request.userAnswers, taxYear, businessId, request.userType)
        ))

      Ok(view(OtherExpensesCYAPage.toString, taxYear, request.userType, summaryList, OtherExpensesCYAController.onSubmit(taxYear, businessId)))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getAnswers andThen requireAnswers).async {
    implicit request =>
      val context = JourneyContextWithNino(taxYear, request.nino, businessId, request.mtditid, ExpensesOtherExpenses)
      val result  = service.submitAnswers[OtherExpensesJourneyAnswers](context, request.userAnswers)

      handleSubmitAnswersResult(context, result)
  }

}
