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

package controllers.journeys.expenses.professionalFees

import cats.data.EitherT
import controllers.actions._
import controllers.{handleResult, standard}
import forms.expenses.professionalFees.ProfessionalFeesAmountFormProvider
import models.Mode
import models.common.{AccountingType, BusinessId, TaxYear}
import navigation.ExpensesNavigator
import pages.expenses.professionalFees.ProfessionalFeesAmountPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.expenses.professionalFees.ProfessionalFeesAmountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ProfessionalFeesAmountController @Inject() (override val messagesApi: MessagesApi,
                                                  selfEmploymentService: SelfEmploymentService,
                                                  navigator: ExpensesNavigator,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  formProvider: ProfessionalFeesAmountFormProvider,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: ProfessionalFeesAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      val result = for {
        accountingTypeStr <- EitherT(selfEmploymentService.getAccountingType(request.user.nino, businessId, request.user.mtditid))
        accountingType = AccountingType.withName(accountingTypeStr.toUpperCase())
        userType       = request.userType
        userAnswers    = request.userAnswers.get(ProfessionalFeesAmountPage, Some(businessId))
        form           = formProvider(userType)
        preparedForm   = userAnswers.fold(form)(form.fill)
      } yield Ok(view(preparedForm, mode, userType, taxYear, businessId, accountingType))

      handleResult(result.value)
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleForm(accountingType: AccountingType): Future[Result] =
        formProvider(request.userType)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, accountingType))),
            value => handleSuccess(value)
          )

      def handleSuccess(value: BigDecimal): Future[Result] =
        selfEmploymentService
          .persistAnswer(businessId, request.userAnswers, value, ProfessionalFeesAmountPage)
          .map(updated => Redirect(navigator.nextPage(ProfessionalFeesAmountPage, mode, updated, taxYear, businessId)))

      selfEmploymentService.getAccountingType(request.user.nino, businessId, request.user.mtditid) flatMap {
        case Left(_)               => Future.successful(Redirect(standard.routes.JourneyRecoveryController.onPageLoad()))
        case Right(accountingType) => handleForm(AccountingType.withName(accountingType))
      }
  }
}
