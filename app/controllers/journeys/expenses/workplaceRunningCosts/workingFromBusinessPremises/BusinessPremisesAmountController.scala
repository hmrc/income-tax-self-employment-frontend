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

package controllers.journeys.expenses.workplaceRunningCosts.workingFromBusinessPremises

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.handleApiResult
import forms.expenses.workplaceRunningCosts.workingFromBusinessPremises.BusinessPremisesAmountFormProvider
import models.Mode
import models.common.{AccountingType, BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import navigation.ExpensesNavigator
import pages.expenses.workplaceRunningCosts.workingFromBusinessPremises.BusinessPremisesAmountPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.expenses.workplaceRunningCosts.workingFromBusinessPremises.BusinessPremisesAmountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessPremisesAmountController @Inject() (override val messagesApi: MessagesApi,
                                                  selfEmploymentService: SelfEmploymentService,
                                                  navigator: ExpensesNavigator,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  formProvider: BusinessPremisesAmountFormProvider,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: BusinessPremisesAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        accountingType <- handleApiResult(selfEmploymentService.getAccountingType(request.nino, businessId, request.mtditid))
        userType       = request.userType
        userAnswers    = request.userAnswers
        existingAnswer = userAnswers.get(BusinessPremisesAmountPage, Some(businessId))
        form           = formProvider(userType)
        preparedForm   = existingAnswer.fold(form)(form.fill)
      } yield Ok(view(preparedForm, mode, userType, taxYear, businessId, accountingType))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      def handleSuccess(userAnswers: UserAnswers, value: BigDecimal): Future[Result] =
        selfEmploymentService
          .persistAnswer(businessId, userAnswers, value, BusinessPremisesAmountPage)
          .map(updated => Redirect(navigator.nextPage(BusinessPremisesAmountPage, mode, updated, taxYear, businessId)))

      def handleForm(form: Form[BigDecimal],
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
        result <- handleForm(form, userType, accountingType, userAnswers).merge
      } yield result
  }

}
