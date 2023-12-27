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

import cats.data.EitherT
import cats.implicits.catsSyntaxOptionId
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.handleServiceCall
import forms.expenses.otherExpenses.OtherExpensesAmountFormProvider
import models.Mode
import models.common.{AccountingType, BusinessId, TaxYear}
import models.journeys.expenses.individualCategories.OtherExpenses
import navigation.ExpensesNavigator
import pages.expenses.otherExpenses.OtherExpensesAmountPage
import pages.expenses.tailoring.individualCategories.OtherExpensesPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentServiceBase
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.expenses.otherExpenses.OtherExpensesAmountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OtherExpensesAmountController @Inject() (override val messagesApi: MessagesApi,
                                               service: SelfEmploymentServiceBase,
                                               navigator: ExpensesNavigator,
                                               identify: IdentifierAction,
                                               getAnswers: DataRetrievalAction,
                                               requireAnswers: DataRequiredAction,
                                               formProvider: OtherExpensesAmountFormProvider,
                                               val controllerComponents: MessagesControllerComponents,
                                               view: OtherExpensesAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] =
    (identify andThen getAnswers andThen requireAnswers) async { implicit request =>
      val resultT = for {
        tailoringAnswer <- EitherT.fromEither[Future](request.valueOrRedirectDefault(OtherExpensesPage, businessId))
        accountingType  <- handleServiceCall(service.getAccountingType(request.user.nino, businessId, request.user.mtditid))
        form = request.userAnswers
          .get(OtherExpensesAmountPage, Some(businessId))
          .fold(formProvider(request.userType))(formProvider(request.userType).fill)
      } yield Ok(view(form, mode, request.userType, AccountingType.withName(accountingType), tailoringAnswer, taxYear, businessId))

      resultT.merge
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] =
    (identify andThen getAnswers andThen requireAnswers) async { implicit request =>
      def handleForm(accountingType: AccountingType, answer: OtherExpenses): Future[Result] =
        formProvider(request.userType)
          .bindFromRequest()
          .fold(
            formErrors => Future.successful(BadRequest(view(formErrors, mode, request.userType, accountingType, answer, taxYear, businessId))),
            value => handleSuccess(value, accountingType)
          )

      def handleSuccess(value: BigDecimal, accountingType: AccountingType): Future[Result] =
        service
          .persistAnswer(businessId, request.userAnswers, value, OtherExpensesAmountPage)
          .map(answer => Redirect(navigator.nextPage(OtherExpensesAmountPage, mode, answer, taxYear, businessId, accountingType.some)))

      val resultT = for {
        tailoringAnswer <- EitherT.fromEither[Future](request.valueOrRedirectDefault(OtherExpensesPage, businessId))
        accountingType  <- handleServiceCall(service.getAccountingType(request.user.nino, businessId, request.user.mtditid))
        result          <- EitherT.right[Result](handleForm(AccountingType.withName(accountingType), tailoringAnswer))
      } yield result

      resultT.merge
    }
}
