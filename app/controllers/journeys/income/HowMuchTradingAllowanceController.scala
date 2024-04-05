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
import forms.income.HowMuchTradingAllowanceFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import pages.income.HowMuchTradingAllowancePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import services.SelfEmploymentService.getMaxTradingAllowance
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.MoneyUtils
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
    with MoneyUtils {

  private val page = HowMuchTradingAllowancePage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      (for {
        allowance <- getMaxTradingAllowance(businessId, request.userAnswers)
        formattedAllowance = formatMoney(allowance, addDecimalForWholeNumbers = false)
        form               = fillForm(page, businessId, formProvider(request.userType, formattedAllowance))
      } yield Ok(view(form, mode, request.userType, taxYear, businessId, formattedAllowance))).merge
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleForm(allowance: String): Future[Result] =
        formProvider(request.userType, allowance)
          .bindFromRequest()
          .fold(
            formErrors => Future.successful(BadRequest(view(formErrors, mode, request.userType, taxYear, businessId, allowance))),
            answer => service.submitGatewayQuestionAndRedirect(page, businessId, request.userAnswers, answer, taxYear, mode)
          )

      (for {
        allowance <- EitherT.fromEither[Future](getMaxTradingAllowance(businessId, request.userAnswers))
        formattedAllowance = formatMoney(allowance, addDecimalForWholeNumbers = false)
        result <- EitherT.right[Result](handleForm(formattedAllowance))
      } yield result).merge
  }
}
