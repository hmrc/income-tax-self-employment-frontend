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

package controllers.journeys.expenses.tailoring.simplifiedExpenses

import cats.implicits.catsSyntaxOptionId
import controllers.actions._
import controllers.journeys.fillForm
import forms.standard.CurrencyFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear, UserType}
import models.common.Journey
import navigation.ExpensesTailoringNavigator
import pages.expenses.tailoring.simplifiedExpenses.TotalExpensesPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.tailoring.simplifiedExpenses.TotalExpensesView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TotalExpensesController @Inject() (override val messagesApi: MessagesApi,
                                         selfEmploymentService: SelfEmploymentService,
                                         navigator: ExpensesTailoringNavigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         hopChecker: HopCheckerAction,
                                         formProvider: CurrencyFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: TotalExpensesView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val page = TotalExpensesPage
  private val form = (userType: UserType) => formProvider(page, userType, prefix = page.toString.some)

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen
      hopChecker.hasPreviousAnswers(Journey.ExpensesTailoring, page, taxYear, businessId, mode)) { implicit request =>
      val filledForm = fillForm(page, businessId, form(request.userType))
      Ok(view(filledForm, mode, request.userType, taxYear, businessId))
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      form(request.userType)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId))),
          value =>
            selfEmploymentService
              .persistAnswer(businessId, request.userAnswers, value, TotalExpensesPage)
              .map(updatedAnswers => Redirect(navigator.nextPage(TotalExpensesPage, mode, updatedAnswers, taxYear, businessId)))
        )
  }

}
