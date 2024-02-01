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

import cats.data.EitherT
import controllers.actions._
import controllers.{handleApiResult, handleResultT}
import forms.expenses.tailoring.individualCategories.FinancialExpensesFormProvider
import models.Mode
import models.common.{AccountingType, BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.errors.ServiceError
import models.journeys.expenses.individualCategories.FinancialExpenses.{Interest, IrrecoverableDebts, OtherFinancialCharges}
import models.journeys.expenses.individualCategories.{
  DisallowableInterest,
  DisallowableIrrecoverableDebts,
  DisallowableOtherFinancialCharges,
  FinancialExpenses
}
import navigation.ExpensesTailoringNavigator
import pages.expenses.tailoring.individualCategories.{
  DisallowableInterestPage,
  DisallowableIrrecoverableDebtsPage,
  DisallowableOtherFinancialChargesPage,
  FinancialExpensesPage
}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.Settable
import services.SelfEmploymentService
import services.SelfEmploymentService.clearDataFromUserAnswers
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.expenses.tailoring.individualCategories.FinancialExpensesView

import javax.inject.{Inject, Singleton}
import scala.annotation.nowarn
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class FinancialExpensesController @Inject() (override val messagesApi: MessagesApi,
                                             selfEmploymentService: SelfEmploymentService,
                                             navigator: ExpensesTailoringNavigator,
                                             identify: IdentifierAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             formProvider: FinancialExpensesFormProvider,
                                             val controllerComponents: MessagesControllerComponents,
                                             view: FinancialExpensesView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      for {
        accountingType <- handleApiResult(selfEmploymentService.getAccountingType(request.nino, businessId, request.mtditid))
        userType       = request.userType
        userAnswers    = request.userAnswers
        existingAnswer = userAnswers.get(FinancialExpensesPage, Some(businessId))
        form           = formProvider(userType)
        preparedForm   = existingAnswer.fold(form)(form.fill)
      } yield Ok(view(preparedForm, mode, userType, taxYear, businessId, accountingType))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleSuccess(userAnswers: UserAnswers, financialExpenses: Set[FinancialExpenses]): Future[Result] =
        for {
          clearedAnswers <- Future.fromTry(clearDependentPageAnswers(userAnswers, Some(businessId), financialExpenses))
          updatedAnswers <- selfEmploymentService.persistAnswer(businessId, clearedAnswers, financialExpenses, FinancialExpensesPage)
        } yield Redirect(navigator.nextPage(FinancialExpensesPage, mode, updatedAnswers, taxYear, businessId))

      def handleForm(form: Form[Set[FinancialExpenses]],
                     userType: UserType,
                     accountingType: AccountingType,
                     userAnswers: UserAnswers): Either[Future[Result], Future[Result]] =
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Left(Future.successful(BadRequest(view(formWithErrors, mode, userType, taxYear, businessId, accountingType)))),
            value => Right(handleSuccess(userAnswers, value))
          )

      for {
        accountingType <- handleApiResult(selfEmploymentService.getAccountingType(request.nino, businessId, request.mtditid))
        userType    = request.userType
        userAnswers = request.userAnswers
        form        = formProvider(userType)
        finalResult <- handleResultT(EitherT.right[ServiceError](handleForm(form, userType, accountingType, userAnswers).merge))
      } yield finalResult
  }

  private def clearDependentPageAnswers(userAnswers: UserAnswers,
                                        businessId: Option[BusinessId],
                                        pageAnswers: Set[FinancialExpenses]): Try[UserAnswers] = {
    @nowarn("msg=match may not be exhaustive")
    def getPageFromAnswer(value: FinancialExpenses): Settable[_] = value match {
      case Interest              => DisallowableInterestPage: Settable[DisallowableInterest]
      case OtherFinancialCharges => DisallowableOtherFinancialChargesPage: Settable[DisallowableOtherFinancialCharges]
      case IrrecoverableDebts    => DisallowableIrrecoverableDebtsPage: Settable[DisallowableIrrecoverableDebts]
    }
    val uncheckedAnswers: List[FinancialExpenses] = List(Interest, OtherFinancialCharges, IrrecoverableDebts).filterNot(pageAnswers.contains(_))
    val pagesToBeRemoved: List[Settable[_]]       = uncheckedAnswers.map(getPageFromAnswer)

    clearDataFromUserAnswers(userAnswers, pagesToBeRemoved, businessId)
  }

}
