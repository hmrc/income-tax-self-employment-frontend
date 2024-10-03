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
import forms.standard.CurrencyFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear, UserType}
import models.errors.ServiceError
import pages.income.TradingAllowanceAmountPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.income.TradingAllowanceAmountView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TradingAllowanceAmountController @Inject() (override val messagesApi: MessagesApi,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  service: SelfEmploymentService,
                                                  formProvider: CurrencyFormProvider,
                                                  view: TradingAllowanceAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val page = TradingAllowanceAmountPage
  private val form = (userType: UserType, turnoverAmount: BigDecimal) =>
    formProvider(
      page,
      userType,
      maxValue = turnoverAmount,
      minValueError = s"tradingAllowanceAmount.error.lessThanZero.$userType",
      maxValueError = s"tradingAllowanceAmount.error.overTurnover.$userType",
      nonNumericError = s"tradingAllowanceAmount.error.nonNumeric.$userType"
    )

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val result = getMaxTradingAllowance(businessId, request.userAnswers).map { allowance =>
        val filledForm = fillForm(page, businessId, form(request.userType, allowance))
        Ok(view(filledForm, mode, request.userType, taxYear, businessId))
      }
      handleResult(result)
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleError(formWithErrors: Form[_]): Result = BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId))
      val result = for {
        allowance <- EitherT.fromEither[Future](getMaxTradingAllowance(businessId, request.userAnswers))
        handleForm <- EitherT.liftF[Future, ServiceError, Result](
          service.defaultHandleForm(form(request.userType, allowance), page, businessId, taxYear, mode, handleError))
      } yield handleForm

      handleResultT(result)
  }

}
