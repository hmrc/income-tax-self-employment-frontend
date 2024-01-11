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

package controllers.journeys.expenses.repairsandmaintenance

import cats.data.EitherT
import controllers.actions._
import controllers.handleResult
import forms.expenses.repairsandmaintenance.RepairsAndMaintenanceAmountFormProvider
import models.Mode
import models.common.{AccountingType, BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.errors.ServiceError
import navigation.ExpensesNavigator
import pages.expenses.repairsandmaintenance.RepairsAndMaintenanceAmountPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.expenses.repairsandmaintenance.RepairsAndMaintenanceAmountView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RepairsAndMaintenanceAmountController @Inject() (override val messagesApi: MessagesApi,
                                                       selfEmploymentService: SelfEmploymentService,
                                                       navigator: ExpensesNavigator,
                                                       identify: IdentifierAction,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       formProvider: RepairsAndMaintenanceAmountFormProvider,
                                                       val controllerComponents: MessagesControllerComponents,
                                                       view: RepairsAndMaintenanceAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val result = for {
        accountingType <- EitherT(selfEmploymentService.getAccountingType(request.user.nino, businessId, request.user.mtditid))
        userType       = request.userType
        userAnswers    = request.userAnswers
        existingAnswer = userAnswers.get(RepairsAndMaintenanceAmountPage, Some(businessId))
        form           = formProvider(userType)
        preparedForm   = existingAnswer.fold(form)(form.fill)
      } yield Ok(view(preparedForm, mode, userType, taxYear, businessId, accountingType))

      handleResult(result.value)

  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      def handleError(formWithErrors: Form[_], userType: UserType, accountingType: AccountingType): Future[Result] =
        Future.successful(
          BadRequest(view(formWithErrors, mode, userType, taxYear, businessId, accountingType))
        )

      def handleSuccess(userAnswers: UserAnswers, value: BigDecimal): Future[Result] =
        selfEmploymentService
          .persistAnswer(businessId, userAnswers, value, RepairsAndMaintenanceAmountPage)
          .map(updated => Redirect(navigator.nextPage(RepairsAndMaintenanceAmountPage, mode, updated, taxYear, businessId)))

      def handleForm(form: Form[BigDecimal],
                     userType: UserType,
                     accountingType: AccountingType,
                     userAnswers: UserAnswers): Either[Future[Result], Future[Result]] =
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Left(handleError(formWithErrors, userType, accountingType)),
            value => Right(handleSuccess(userAnswers, value))
          )

      val result = for {
        accountingType <- EitherT(selfEmploymentService.getAccountingType(request.user.nino, businessId, request.user.mtditid))
        userType    = request.userType
        userAnswers = request.userAnswers
        form        = formProvider(userType)
        finalResult <- EitherT.right[ServiceError](handleForm(form, userType, accountingType, userAnswers).merge)
      } yield finalResult

      handleResult(result.value)
  }

}
