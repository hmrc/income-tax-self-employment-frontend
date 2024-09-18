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
import config.TaxYearConfig.totalIncomeIsEqualOrAboveThreshold
import controllers.actions._
import controllers.{handleApiResult, handleResultT}
import forms.expenses.tailoring.ExpensesCategoriesFormProvider
import models.common.{BusinessId, Journey, TaxYear, UserType}
import models.database.UserAnswers
import models.errors.ServiceError
import models.journeys.expenses.ExpensesTailoring
import models.journeys.expenses.ExpensesTailoring.{IndividualCategories, NoExpenses, TotalAmount, tailoringList}
import models.{Mode, NormalMode}
import navigation.ExpensesTailoringNavigator
import pages.expenses.tailoring._
import pages.expenses.tailoring.simplifiedExpenses._
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import services.SelfEmploymentService.clearDataFromUserAnswers
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
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
    with I18nSupport
    with Logging {

  private val page = ExpensesCategoriesPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen
      hopChecker.hasPreviousAnswers(Journey.ExpensesTailoring, page, taxYear, businessId, mode)).async { implicit request =>
      val ctx = request.mkJourneyNinoContext(taxYear, businessId, Journey.Income)

      val finalResult = for {
        incomeAmount <- selfEmploymentService.getTotalIncome(ctx)
        incomeIsEqualOrAboveThreshold = totalIncomeIsEqualOrAboveThreshold(incomeAmount)
        existingAnswer                = request.getValue(ExpensesCategoriesPage, businessId)
        form                          = formProvider(request.userType)
        preparedForm                  = existingAnswer.fold(form)(form.fill)
        result <-
          if (incomeIsEqualOrAboveThreshold) {
            EitherT.right[ServiceError](persistAndRedirectWhenIncomeIsEqualOrAboveThreshold(request.userAnswers, businessId, taxYear))
          } else {
            EitherT.pure[Future, ServiceError](Ok(view(preparedForm, mode, request.userType, taxYear, businessId)))
          }
      } yield result

      handleApiResult(finalResult)
    }

  private def persistAndRedirectWhenIncomeIsEqualOrAboveThreshold(userAnswers: UserAnswers,
                                                                  businessId: BusinessId,
                                                                  taxYear: TaxYear): Future[Result] =
    persistAndRedirect(userAnswers, IndividualCategories, businessId, taxYear, NormalMode)

  private def persistAndRedirect(userAnswers: UserAnswers,
                                 value: ExpensesTailoring,
                                 businessId: BusinessId,
                                 taxYear: TaxYear,
                                 mode: Mode): Future[Result] =
    for {
      editedUserAnswers <- Future.fromTry(clearDependentPageAnswers(userAnswers, Some(businessId), value))
      result <- selfEmploymentService
        .persistAnswer(businessId, editedUserAnswers, value, ExpensesCategoriesPage)
        .map(updated => Redirect(navigator.nextPage(ExpensesCategoriesPage, mode, updated, taxYear, businessId)))
    } yield result

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleError(formWithErrors: Form[_], userType: UserType): Future[Result] =
        Future.successful(
          BadRequest(view(formWithErrors, mode, userType, taxYear, businessId))
        )

      def handleSuccess(userAnswers: UserAnswers, value: ExpensesTailoring): Future[Result] = {
        val redirectMode = continueAsNormalModeIfPrevAnswerChanged(value)
        persistAndRedirect(userAnswers, value, businessId, taxYear, redirectMode)
      }

      def handleForm(form: Form[ExpensesTailoring], userType: UserType, userAnswers: UserAnswers, incomeIsOverThreshold: Boolean): Future[Result] =
        if (incomeIsOverThreshold) {
          handleSuccess(userAnswers, IndividualCategories)
        } else {
          form
            .bindFromRequest()
            .fold(
              formWithErrors => handleError(formWithErrors, userType),
              value => handleSuccess(userAnswers, value)
            )
        }

      def continueAsNormalModeIfPrevAnswerChanged(value: ExpensesTailoring): Mode = {
        val previousAnswer = request.getValue(ExpensesCategoriesPage, businessId)
        if (previousAnswer.exists(_ != value)) NormalMode else mode
      }

      val ctx = request.mkJourneyNinoContext(taxYear, businessId, Journey.Income)
      val result = for {
        totalIncome <- selfEmploymentService.getTotalIncome(ctx)
        incomeIsEqualOrAboveThreshold = totalIncomeIsEqualOrAboveThreshold(totalIncome)
        userType                      = request.userType
        userAnswers                   = request.userAnswers
        form                          = formProvider(userType)
        finalResult <- EitherT.right[ServiceError](handleForm(form, userType, userAnswers, incomeIsEqualOrAboveThreshold))
      } yield finalResult

      handleResultT(result)
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
