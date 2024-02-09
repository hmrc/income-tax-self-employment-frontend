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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.returnAccountingType
import forms.expenses.otherExpenses.OtherExpensesAmountFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import models.journeys.expenses.individualCategories.OtherExpenses
import navigation.ExpensesNavigator
import pages.expenses.otherExpenses.OtherExpensesAmountPage
import pages.expenses.tailoring.individualCategories.OtherExpensesPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.expenses.otherExpenses.OtherExpensesAmountView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OtherExpensesAmountController @Inject() (override val messagesApi: MessagesApi,
                                               service: SelfEmploymentService,
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
    (identify andThen getAnswers andThen requireAnswers) { implicit request =>
      request.valueOrRedirectDefault[OtherExpenses](OtherExpensesPage, businessId) match {
        case Left(redirect) => redirect
        case Right(tailoringAnswer) =>
          val form = request.userAnswers
            .get(OtherExpensesAmountPage, Some(businessId))
            .fold(formProvider(request.userType))(formProvider(request.userType).fill)
          Ok(view(form, mode, request.userType, returnAccountingType(businessId), tailoringAnswer, taxYear, businessId))
      }
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] =
    (identify andThen getAnswers andThen requireAnswers) async { implicit request =>
      def handleSuccess(value: BigDecimal): Future[Result] =
        service
          .persistAnswer(businessId, request.userAnswers, value, OtherExpensesAmountPage)
          .map(answer => Redirect(navigator.nextPage(OtherExpensesAmountPage, mode, answer, taxYear, businessId)))

      request.valueOrRedirectDefault[OtherExpenses](OtherExpensesPage, businessId) match {
        case Left(redirect) => Future(redirect)
        case Right(tailoringAnswer) =>
          formProvider(request.userType)
            .bindFromRequest()
            .fold(
              formErrors =>
                Future.successful(
                  BadRequest(view(formErrors, mode, request.userType, returnAccountingType(businessId), tailoringAnswer, taxYear, businessId))),
              value => handleSuccess(value)
            )
      }
    }
}
