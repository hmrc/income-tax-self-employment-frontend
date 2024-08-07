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

package controllers.journeys.expenses.otherExpenses

import cats.implicits.toTraverseOps
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.journeys.fillForm
import forms.expenses.otherExpenses.OtherExpensesDisallowableAmountFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import pages.expenses.otherExpenses.{OtherExpensesAmountPage, OtherExpensesDisallowableAmountPage}
import play.api.data.Form
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
                                                           identify: IdentifierAction,
                                                           getAnswers: DataRetrievalAction,
                                                           requireAnswers: DataRequiredAction,
                                                           service: SelfEmploymentService,
                                                           formProvider: OtherExpensesDisallowableAmountFormProvider,
                                                           val controllerComponents: MessagesControllerComponents,
                                                           view: OtherExpensesDisallowableAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val page = OtherExpensesDisallowableAmountPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getAnswers andThen requireAnswers) {
    implicit request =>
      (for {
        amount <- request.valueOrRedirectDefault(OtherExpensesAmountPage, businessId)
        form = fillForm(page, businessId, formProvider(request.userType, amount))
      } yield Ok(view(form, mode, taxYear, businessId, request.userType, formatMoney(amount)))).merge
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] =
    (identify andThen getAnswers andThen requireAnswers).async { implicit request =>
      def handleError(amount: BigDecimal)(formWithErrors: Form[_]): Result =
        BadRequest(view(formWithErrors, mode, taxYear, businessId, request.userType, formatMoney(amount)))
      def handleForm(amount: BigDecimal): Future[Result] =
        service.defaultHandleForm(formProvider(request.userType, amount), page, businessId, taxYear, mode, handleError(amount))

      request.valueOrRedirectDefault(OtherExpensesAmountPage, businessId).traverse(handleForm).map(_.merge)
    }

}
