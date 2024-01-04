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

package controllers.journeys.expenses.financialCharges

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.handleSubmitAnswersResult
import controllers.journeys.expenses.financialCharges.routes._
import models.common.{BusinessId, JourneyContextWithNino, TaxYear}
import models.journeys.Journey.ExpensesFinancialCharges
import models.journeys.expenses.financialCharges.FinancialChargesJourneyAnswers
import pages.expenses.financialCharges.FinancialChargesCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentServiceBase
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.checkAnswers.expenses.financialCharges.{FinancialChargesAmountSummary, FinancialChargesDisallowableAmountSummary}
import viewmodels.journeys.SummaryListCYA.summaryListOpt
import views.html.standard.CheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class FinancialChargesCYAController @Inject() (override val messagesApi: MessagesApi,
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
          FinancialChargesAmountSummary.row(request.userAnswers, taxYear, businessId, request.userType),
          FinancialChargesDisallowableAmountSummary.row(request.userAnswers, taxYear, businessId, request.userType)
        ))

      Ok(
        view(FinancialChargesCYAPage.toString, taxYear, request.userType, summaryList, FinancialChargesCYAController.onSubmit(taxYear, businessId))
      )
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getAnswers andThen requireAnswers) async {
    implicit request =>
      val context = JourneyContextWithNino(taxYear, request.nino, businessId, request.mtditid, ExpensesFinancialCharges)
      val result  = service.submitAnswers[FinancialChargesJourneyAnswers](context, request.userAnswers)

      handleSubmitAnswersResult(context, result)
  }

}
