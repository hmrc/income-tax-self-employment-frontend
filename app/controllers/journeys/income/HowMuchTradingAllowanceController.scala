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

package controllers.journeys.income

import cats.data.EitherT
import controllers.actions._
import controllers.journeys.fillForm
import controllers.{handleResult, handleResultT}
import forms.income.HowMuchTradingAllowanceFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import models.errors.ServiceError
import models.journeys.income.HowMuchTradingAllowance
import pages.income.HowMuchTradingAllowancePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Logging, MoneyUtils}
import views.html.journeys.income.HowMuchTradingAllowanceView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HowMuchTradingAllowanceController @Inject() (override val messagesApi: MessagesApi,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   service: SelfEmploymentService,
                                                   formProvider: HowMuchTradingAllowanceFormProvider,
                                                   view: HowMuchTradingAllowanceView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with MoneyUtils
    with Logging {

  private val page = HowMuchTradingAllowancePage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val result = getMaxTradingAllowance(businessId, request.userAnswers).map { allowance =>
        val formattedAllowance = formatMoney(allowance, addDecimalForWholeNumbers = false)
        val filledForm         = fillForm(page, businessId, formProvider(request.userType, formattedAllowance))
        Ok(view(filledForm, mode, request.userType, taxYear, businessId, formattedAllowance))
      }
      handleResult(result)
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleError(allowance: String)(formWithErrors: Form[_]): Result =
        BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, allowance))
      def handleSuccess(answer: HowMuchTradingAllowance): Future[Result] =
        service.submitGatewayQuestionAndRedirect(page, businessId, request.userAnswers, answer, taxYear, mode)

      val result = for {
        allowance <- EitherT.fromEither[Future](getMaxTradingAllowance(businessId, request.userAnswers))
        formattedAllowance = formatMoney(allowance, addDecimalForWholeNumbers = false)
        handleForm <- EitherT.liftF[Future, ServiceError, Result](
          service.handleForm(formProvider(request.userType, formattedAllowance), handleError(formattedAllowance), handleSuccess))
      } yield handleForm

      handleResultT(result)
  }
}
