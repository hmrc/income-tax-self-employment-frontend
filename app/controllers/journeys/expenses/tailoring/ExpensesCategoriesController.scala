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
import models.Mode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.expenses.ExpensesTailoring
import models.journeys.expenses.ExpensesTailoring.{IndividualCategories, NoExpenses, TotalAmount}
import navigation.ExpensesTailoringNavigator
import pages.expenses.simplifiedExpenses.TotalExpensesPage
import pages.expenses.tailoring._
import pages.income.TurnoverIncomeAmountPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.Settable
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.tailoring.ExpensesCategoriesView

import javax.inject.Inject
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class ExpensesCategoriesController @Inject() (override val messagesApi: MessagesApi,
                                              selfEmploymentService: SelfEmploymentService,
                                              navigator: ExpensesTailoringNavigator,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              formProvider: ExpensesCategoriesFormProvider,
                                              val controllerComponents: MessagesControllerComponents,
                                              view: ExpensesCategoriesView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val incomeThreshold: BigDecimal = 85000

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
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

      def handleSuccess(userAnswers: UserAnswers, value: ExpensesTailoring, incomeIsOverThreshold: Boolean): Future[Result] = {
        val answer = if (incomeIsOverThreshold) IndividualCategories else value
        for {
          editedUserAnswers <- Future.fromTry(clearDataFromUserAnswers(userAnswers, Some(businessId), answer))
          result <- selfEmploymentService
            .saveAnswer(businessId, editedUserAnswers, answer, ExpensesCategoriesPage)
            .map(updated => Redirect(navigator.nextPage(ExpensesCategoriesPage, mode, updated, taxYear, businessId)))
        } yield result
      }

      def handleForm(form: Form[ExpensesTailoring], userType: UserType, userAnswers: UserAnswers, incomeIsOverThreshold: Boolean): Future[Result] =
        form
          .bindFromRequest()
          .fold(
            formWithErrors => handleError(formWithErrors, userType, incomeIsOverThreshold),
            value => handleSuccess(userAnswers, value, incomeIsOverThreshold)
          )

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

  private def clearDataFromUserAnswers(userAnswers: UserAnswers, businessId: Option[BusinessId], pageAnswer: ExpensesTailoring): Try[UserAnswers] = {

    val tailoringList = List(
      AdvertisingOrMarketingPage,
      DepreciationPage,
      DisallowableIrrecoverableDebtsPage,
      DisallowableOtherFinancialChargesPage,
      DisallowableInterestPage,
      DisallowableStaffCostsPage,
      DisallowableSubcontractorCostsPage,
      DisallowableProfessionalFeesPage,
      EntertainmentCostsPage,
      FinancialExpensesPage,
      GoodsToSellOrUsePage,
      OfficeSuppliesPage,
      OtherExpensesPage,
      ProfessionalServiceExpensesPage,
      RepairsAndMaintenancePage,
      TaxiMinicabOrRoadHaulagePage,
      TravelForWorkPage,
      WorkFromHomePage,
      WorkFromBusinessPremisesPage,
      TotalExpensesPage
    )

    val toBeRemoved = pageAnswer match {
      case NoExpenses           => tailoringList
      case IndividualCategories => tailoringList.filter(_ == TotalExpensesPage)
      case TotalAmount          => tailoringList.filterNot(_ == TotalExpensesPage)
    }

    @tailrec
    def removeData(userAnswers: UserAnswers, pages: List[Settable[_]]): Try[UserAnswers] =
      pages match {
        case Nil =>
          Try(userAnswers)
        case head :: tail =>
          userAnswers.remove(head, businessId) match {
            case Success(updatedUserAnswers) =>
              removeData(updatedUserAnswers, tail)
            case Failure(exception) =>
              Failure(exception)
          }
      }

    removeData(userAnswers, toBeRemoved)
  }
}
