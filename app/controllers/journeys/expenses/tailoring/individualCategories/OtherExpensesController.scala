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

package controllers.journeys.expenses.tailoring.individualCategories

import controllers.actions._
import controllers.journeys.{clearDependentPages, fillForm}
import forms.standard.EnumerableFormProvider
import models.common.Journey.ExpensesOtherExpenses
import models.common.{BusinessId, Journey, TaxYear, UserType}
import models.journeys.expenses.individualCategories.OtherExpenses
import models.journeys.expenses.individualCategories.OtherExpenses.enumerable
import models.{CheckMode, Mode}
import navigation.ExpensesTailoringNavigator
import pages.expenses.tailoring.individualCategories.OtherExpensesPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.tailoring.individualCategories.OtherExpensesView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OtherExpensesController @Inject() (override val messagesApi: MessagesApi,
                                         selfEmploymentService: SelfEmploymentService,
                                         navigator: ExpensesTailoringNavigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         hopChecker: HopCheckerAction,
                                         formProvider: EnumerableFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: OtherExpensesView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  private val page = OtherExpensesPage
  private val form = (userType: UserType) => formProvider[OtherExpenses](page, userType)

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
            for {
              updatedAnswers <-
                if (mode == CheckMode && !request.userAnswers.get(OtherExpensesPage, businessId).contains(value)) {
                  selfEmploymentService.clearExpensesData(taxYear, businessId, ExpensesOtherExpenses)
                  clearDependentPages(OtherExpensesPage, value, request.userAnswers, businessId)
                } else {
                  Future.successful(request.userAnswers)
                }
              savedAnswers <- selfEmploymentService.persistAnswer(businessId, updatedAnswers, value, OtherExpensesPage)
            } yield Redirect(navigator.nextPage(OtherExpensesPage, mode, savedAnswers, taxYear, businessId))
        )
  }

}
