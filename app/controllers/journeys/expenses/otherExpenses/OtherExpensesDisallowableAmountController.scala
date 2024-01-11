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

import cats.implicits.{catsSyntaxOptionId, toTraverseOps}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.expenses.otherExpenses.OtherExpensesDisallowableAmountFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import navigation.ExpensesNavigator
import pages.expenses.otherExpenses.{OtherExpensesAmountPage, OtherExpensesDisallowableAmountPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.MoneyUtils.formatMoney
import views.html.journeys.expenses.otherExpenses.OtherExpensesDisallowableAmountView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OtherExpensesDisallowableAmountController @Inject() (override val messagesApi: MessagesApi,
                                                           navigator: ExpensesNavigator,
                                                           identify: IdentifierAction,
                                                           getAnswers: DataRetrievalAction,
                                                           requireAnswers: DataRequiredAction,
                                                           service: SelfEmploymentService,
                                                           formProvider: OtherExpensesDisallowableAmountFormProvider,
                                                           val controllerComponents: MessagesControllerComponents,
                                                           view: OtherExpensesDisallowableAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getAnswers andThen requireAnswers) {
    implicit request =>
      (for {
        amount <- request.valueOrRedirectDefault(OtherExpensesAmountPage, businessId)
        form = request.userAnswers
          .get(OtherExpensesDisallowableAmountPage, businessId.some)
          .fold(formProvider(request.userType, amount))(formProvider(request.userType, amount).fill)
      } yield Ok(view(form, mode, taxYear, businessId, request.userType, formatMoney(amount)))).merge
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] =
    (identify andThen getAnswers andThen requireAnswers).async { implicit request =>
      def handleForm(amount: BigDecimal): Future[Result] =
        formProvider(request.userType, amount)
          .bindFromRequest()
          .fold(
            formErrors => Future.successful(BadRequest(view(formErrors, mode, taxYear, businessId, request.userType, formatMoney(amount)))),
            answer => handleSuccess(answer)
          )

      def handleSuccess(answer: BigDecimal): Future[Result] =
        service
          .persistAnswer(businessId, request.userAnswers, answer, OtherExpensesDisallowableAmountPage)
          .map(answer => Redirect(navigator.nextPage(OtherExpensesDisallowableAmountPage, mode, answer, taxYear, businessId)))

      request
        .valueOrRedirectDefault(OtherExpensesAmountPage, businessId)
        .traverse(handleForm)
        .map(_.merge)
    }

}
