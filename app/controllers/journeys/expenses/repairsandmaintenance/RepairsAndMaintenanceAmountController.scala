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

package controllers.journeys.expenses.repairsandmaintenance

import cats.data.EitherT
import controllers.actions._
import controllers.handleResult
import forms.expenses.repairsandmaintenance.RepairsAndMaintenanceAmountFormProvider
import models.Mode
import models.common.{AccountingType, BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.errors.HttpError
import navigation.ExpensesNavigator
import pages.expenses.repairsandmaintenance.RepairsAndMaintenanceAmountPage
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentServiceBase
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.repairsandmaintenance.RepairsAndMaintenanceAmountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RepairsAndMaintenanceAmountController @Inject() (
    override val messagesApi: MessagesApi,
    selfEmploymentService: SelfEmploymentServiceBase,
    navigator: ExpensesNavigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    formProvider: RepairsAndMaintenanceAmountFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: RepairsAndMaintenanceAmountView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  private implicit val logger: Logger = Logger(this.getClass)

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    val result = for {
      accountingTypeStr <- EitherT(selfEmploymentService.getAccountingType(request.user.nino, businessId, request.user.mtditid))
      accountingType = AccountingType.withName(accountingTypeStr.toUpperCase())
      userType       = request.userType
      userAnswers    = request.userAnswers.getOrElse(UserAnswers(request.userId))
      existingAnswer = userAnswers.get(RepairsAndMaintenanceAmountPage, Some(businessId.value))
      form           = formProvider(userType)
      preparedForm   = existingAnswer.fold(form)(form.fill)
    } yield Ok(view(preparedForm, mode, userType, taxYear, businessId, accountingType))

    handleResult(result.value)

  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    def handleError(formWithErrors: Form[_], userType: UserType, accountingType: AccountingType) =
      Future.successful(
        BadRequest(view(formWithErrors, mode, userType, taxYear, businessId, accountingType))
      )

    def handleSuccess(userAnswers: UserAnswers, value: BigDecimal) =
      selfEmploymentService
        .saveAnswer(businessId, userAnswers, value, RepairsAndMaintenanceAmountPage)
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
      accountingTypeStr <- EitherT(selfEmploymentService.getAccountingType(request.user.nino, businessId, request.user.mtditid))
      accountingType = AccountingType.withName(accountingTypeStr.toUpperCase())
      userType       = request.userType
      userAnswers    = request.userAnswers.getOrElse(UserAnswers(request.userId))
      form           = formProvider(userType)
      finalResult <- EitherT.right[HttpError](handleForm(form, userType, accountingType, userAnswers).merge)
    } yield finalResult

    handleResult(result.value)
  }

}
