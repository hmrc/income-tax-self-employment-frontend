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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction, SubmittedDataRetrievalActionProvider}
import controllers.journeys.expenses.financialCharges.routes._
import models.common.{BusinessId, TaxYear}
import models.journeys.Journey.ExpensesFinancialCharges
import models.journeys.expenses.financialCharges.FinancialChargesJourneyAnswers
import pages.expenses.financialCharges.FinancialChargesCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.expenses.financialCharges.{FinancialChargesAmountSummary, FinancialChargesDisallowableAmountSummary}
import viewmodels.journeys.SummaryListCYA.summaryListOpt
import views.html.standard.CheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class FinancialChargesCYAController @Inject() (override val messagesApi: MessagesApi,
                                               val controllerComponents: MessagesControllerComponents,
                                               identify: IdentifierAction,
                                               getUserAnswers: DataRetrievalAction,
                                               getJourneyAnswers: SubmittedDataRetrievalActionProvider,
                                               requireAnswers: DataRequiredAction,
                                               view: CheckYourAnswersView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] =
    (identify andThen getUserAnswers andThen getJourneyAnswers[FinancialChargesJourneyAnswers](req =>
      req.mkJourneyNinoContext(taxYear, businessId, ExpensesFinancialCharges)) andThen requireAnswers) { implicit request =>
      val summaryList = summaryListOpt(
        List(
          FinancialChargesAmountSummary.row(request.userAnswers, taxYear, businessId, request.userType),
          FinancialChargesDisallowableAmountSummary.row(request.userAnswers, taxYear, businessId, request.userType)
        ))

      Ok(
        view(FinancialChargesCYAPage.toString, taxYear, request.userType, summaryList, FinancialChargesCYAController.onPageLoad(taxYear, businessId))
      )
    }

  // TODO Implement Save & Continue in SASS-6211
  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getUserAnswers andThen requireAnswers) { _ =>
    Redirect(FinancialChargesCYAController.onPageLoad(taxYear, businessId))
  }

}
