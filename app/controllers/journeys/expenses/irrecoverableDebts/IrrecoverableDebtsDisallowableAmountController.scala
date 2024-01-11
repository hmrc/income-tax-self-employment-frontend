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

import cats.data.EitherT
import controllers.actions._
import forms.expenses.irrecoverableDebts.IrrecoverableDebtsDisallowableAmountFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import navigation.ExpensesNavigator
import pages.expenses.irrecoverableDebts.{IrrecoverableDebtsAmountPage, IrrecoverableDebtsDisallowableAmountPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.MoneyUtils
import views.html.journeys.expenses.irrecoverableDebts.IrrecoverableDebtsDisallowableAmountView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IrrecoverableDebtsDisallowableAmountController @Inject() (override val messagesApi: MessagesApi,
                                                                selfEmploymentService: SelfEmploymentService,
                                                                navigator: ExpensesNavigator,
                                                                identify: IdentifierAction,
                                                                getData: DataRetrievalAction,
                                                                requireData: DataRequiredAction,
                                                                formProvider: IrrecoverableDebtsDisallowableAmountFormProvider,
                                                                val controllerComponents: MessagesControllerComponents,
                                                                view: IrrecoverableDebtsDisallowableAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with MoneyUtils {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      (for {
        allowableAmount <- request.valueOrRedirectDefault(IrrecoverableDebtsAmountPage, businessId)
        existingAnswer = request.getValue(IrrecoverableDebtsDisallowableAmountPage, businessId)
        form           = formProvider(request.userType, allowableAmount)
        preparedForm   = existingAnswer.fold(form)(form.fill)
      } yield Ok(view(preparedForm, mode, request.userType, taxYear, businessId, formatMoney(allowableAmount)))).merge
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      def handleForm(form: Form[BigDecimal], userType: UserType, userAnswers: UserAnswers, allowableAmount: BigDecimal): Future[Result] =
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future(BadRequest(view(formWithErrors, mode, userType, taxYear, businessId, formatMoney(allowableAmount)))),
            value => handleSuccess(userAnswers, value)
          )

      def handleSuccess(userAnswers: UserAnswers, value: BigDecimal): Future[Result] =
        selfEmploymentService
          .persistAnswer(businessId, userAnswers, value, IrrecoverableDebtsDisallowableAmountPage)
          .map(updated => Redirect(navigator.nextPage(IrrecoverableDebtsDisallowableAmountPage, mode, updated, taxYear, businessId)))

      val getAllowableValue = request.valueOrRedirectDefault(IrrecoverableDebtsAmountPage, businessId)
      val result = for {
        allowableAmount <- EitherT.fromEither[Future](getAllowableValue)
        userType    = request.userType
        userAnswers = request.userAnswers
        form        = formProvider(userType, allowableAmount)
        finalResult <- EitherT.right[Result](handleForm(form, userType, userAnswers, allowableAmount))
      } yield finalResult

      result.merge
  }
}
