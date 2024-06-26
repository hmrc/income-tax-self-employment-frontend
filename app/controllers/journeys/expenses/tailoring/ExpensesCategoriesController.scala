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

package controllers.journeys.expenses.tailoring

import cats.data.EitherT
import controllers.actions._
import forms.expenses.tailoring.ExpensesCategoriesFormProvider
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.Journey
import models.journeys.expenses.ExpensesTailoring
import models.journeys.expenses.ExpensesTailoring.{IndividualCategories, NoExpenses, TotalAmount, tailoringList}
import models.{Mode, NormalMode}
import navigation.ExpensesTailoringNavigator
import pages.expenses.tailoring._
import pages.expenses.tailoring.simplifiedExpenses._
import pages.income.TurnoverIncomeAmountPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import services.SelfEmploymentService.clearDataFromUserAnswers
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.tailoring.ExpensesCategoriesView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class ExpensesCategoriesController @Inject() (override val messagesApi: MessagesApi,
                                              selfEmploymentService: SelfEmploymentService,
                                              navigator: ExpensesTailoringNavigator,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              hopChecker: HopCheckerAction,
                                              formProvider: ExpensesCategoriesFormProvider,
                                              val controllerComponents: MessagesControllerComponents,
                                              view: ExpensesCategoriesView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  private val page = ExpensesCategoriesPage

  private val incomeThreshold: BigDecimal = 85000

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen
      hopChecker.hasPreviousAnswers(Journey.ExpensesTailoring, page, taxYear, businessId, mode)) { implicit request =>
      (for {
        incomeAmount <- request.valueOrRedirectDefault(TurnoverIncomeAmountPage, businessId)
        incomeIsOverThreshold = incomeAmount > incomeThreshold
        existingAnswer        = request.getValue(ExpensesCategoriesPage, businessId)
        form                  = formProvider(request.userType)
        preparedForm          = existingAnswer.fold(form)(form.fill)
      } yield Ok(view(preparedForm, mode, request.userType, taxYear, businessId, incomeIsOverThreshold))).merge
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleError(formWithErrors: Form[_], userType: UserType, incomeIsOverThreshold: Boolean): Future[Result] =
        Future.successful(
          BadRequest(view(formWithErrors, mode, userType, taxYear, businessId, incomeIsOverThreshold))
        )

      def handleSuccess(userAnswers: UserAnswers, value: ExpensesTailoring): Future[Result] = {
        val redirectMode = continueAsNormalModeIfPrevAnswerChanged(value)
        for {
          editedUserAnswers <- Future.fromTry(clearDependentPageAnswers(userAnswers, Some(businessId), value))
          result <- selfEmploymentService
            .persistAnswer(businessId, editedUserAnswers, value, ExpensesCategoriesPage)
            .map(updated => Redirect(navigator.nextPage(ExpensesCategoriesPage, redirectMode, updated, taxYear, businessId)))
        } yield result
      }

      def handleForm(form: Form[ExpensesTailoring], userType: UserType, userAnswers: UserAnswers, incomeIsOverThreshold: Boolean): Future[Result] =
        if (incomeIsOverThreshold) {
          handleSuccess(userAnswers, IndividualCategories)
        } else {
          form
            .bindFromRequest()
            .fold(
              formWithErrors => handleError(formWithErrors, userType, incomeIsOverThreshold),
              value => handleSuccess(userAnswers, value)
            )
        }

      def continueAsNormalModeIfPrevAnswerChanged(value: ExpensesTailoring): Mode = {
        val previousAnswer = request.getValue(ExpensesCategoriesPage, businessId)
        if (previousAnswer.exists(_ != value)) NormalMode else mode
      }

      val result = for {
        incomeAmount <- EitherT.fromEither[Future](request.valueOrRedirectDefault(TurnoverIncomeAmountPage, businessId))
        incomeIsOverThreshold = incomeAmount > incomeThreshold
        userType              = request.userType
        userAnswers           = request.userAnswers
        form                  = formProvider(userType)
        finalResult <- EitherT.right[Result](handleForm(form, userType, userAnswers, incomeIsOverThreshold))
      } yield finalResult

      result.merge
  }

  private def clearDependentPageAnswers(userAnswers: UserAnswers, businessId: Option[BusinessId], pageAnswer: ExpensesTailoring): Try[UserAnswers] = {
    val toBeRemoved = pageAnswer match {
      case NoExpenses           => tailoringList
      case IndividualCategories => tailoringList.filter(_ == TotalExpensesPage)
      case TotalAmount          => tailoringList.filterNot(_ == TotalExpensesPage)
    }
    clearDataFromUserAnswers(userAnswers, toBeRemoved, businessId)
  }
}
