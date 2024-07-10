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

import cats.implicits.catsSyntaxOptionId
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.journeys.fillForm
import controllers.returnAccountingType
import forms.standard.CurrencyFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear, UserType}
import models.journeys.expenses.individualCategories.OtherExpenses
import pages.expenses.otherExpenses.OtherExpensesAmountPage
import pages.expenses.tailoring.individualCategories.OtherExpensesPage
import play.api.data.Form
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
                                               identify: IdentifierAction,
                                               getAnswers: DataRetrievalAction,
                                               requireAnswers: DataRequiredAction,
                                               formProvider: CurrencyFormProvider,
                                               val controllerComponents: MessagesControllerComponents,
                                               view: OtherExpensesAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val page = OtherExpensesAmountPage
  private val form = (userType: UserType) => formProvider(page, userType, prefix = page.toString.some)

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] =
    (identify andThen getAnswers andThen requireAnswers) { implicit request =>
      request.valueOrRedirectDefault[OtherExpenses](OtherExpensesPage, businessId) match {
        case Left(redirect) => redirect
        case Right(tailoringAnswer) =>
          val filledForm = fillForm(page, businessId, form(request.userType))
          Ok(view(filledForm, mode, request.userType, returnAccountingType(businessId), tailoringAnswer, taxYear, businessId))
      }
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] =
    (identify andThen getAnswers andThen requireAnswers) async { implicit request =>
      def handleError(tailoringAnswer: OtherExpenses)(formWithErrors: Form[_]): Result =
        BadRequest(view(formWithErrors, mode, request.userType, returnAccountingType(businessId), tailoringAnswer, taxYear, businessId))

      request.valueOrRedirectDefault[OtherExpenses](OtherExpensesPage, businessId) match {
        case Left(redirect) => Future(redirect)
        case Right(tailoringAnswer) =>
          service.defaultHandleForm(formProvider(page, request.userType), page, businessId, taxYear, mode, handleError(tailoringAnswer))
      }
    }
}
