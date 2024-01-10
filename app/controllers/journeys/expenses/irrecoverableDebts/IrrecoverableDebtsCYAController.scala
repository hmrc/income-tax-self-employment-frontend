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

package controllers.journeys.expenses.irrecoverableDebts

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.common.{BusinessId, TaxYear}
import pages.expenses.irrecoverableDebts.IrrecoverableDebtsCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.expenses.irrecoverableDebts.{IrrecoverableDebtsAmountSummary, IrrecoverableDebtsDisallowableAmountSummary}
import viewmodels.journeys.SummaryListCYA.summaryListOpt
import views.html.standard.CheckYourAnswersView

import javax.inject.{Inject, Singleton}

@Singleton
class IrrecoverableDebtsCYAController @Inject() (override val messagesApi: MessagesApi,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 identify: IdentifierAction,
                                                 getAnswers: DataRetrievalAction,
                                                 requireAnswers: DataRequiredAction,
                                                 view: CheckYourAnswersView)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getAnswers andThen requireAnswers) {
    implicit request =>
      val summaryList = summaryListOpt(
        List(
          IrrecoverableDebtsAmountSummary.row(request.userAnswers, taxYear, businessId, request.userType),
          IrrecoverableDebtsDisallowableAmountSummary.row(request.userAnswers, taxYear, businessId, request.userType)
        ))

      Ok(
        view(
          IrrecoverableDebtsCYAPage.toString,
          taxYear,
          request.userType,
          summaryList,
          routes.IrrecoverableDebtsCYAController.onSubmit(taxYear, businessId))
      )
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getAnswers andThen requireAnswers) { _ =>
    Redirect(routes.IrrecoverableDebtsCYAController.onPageLoad(taxYear, businessId))
  }

}
