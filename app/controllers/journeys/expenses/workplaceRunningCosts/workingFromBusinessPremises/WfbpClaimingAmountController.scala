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

import cats.data.EitherT
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.expenses.workplaceRunningCosts.workingFromBusinessPremises.WfbpClaimingAmountFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import navigation.WorkplaceRunningCostsNavigator
import pages.expenses.workplaceRunningCosts.workingFromBusinessPremises.{BusinessPremisesAmountPage, WfbpClaimingAmountPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.workplaceRunningCosts.workingFromBusinessPremises.WfbpClaimingAmountView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WfbpClaimingAmountController @Inject() (override val messagesApi: MessagesApi,
                                              service: SelfEmploymentService,
                                              navigator: WorkplaceRunningCostsNavigator,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              formProvider: WfbpClaimingAmountFormProvider,
                                              val controllerComponents: MessagesControllerComponents,
                                              view: WfbpClaimingAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      (for {
        bpExpensesAmount <- request.valueOrRedirectDefault(BusinessPremisesAmountPage, businessId)
        existingAnswer = request.getValue(WfbpClaimingAmountPage, businessId)
        form           = formProvider(request.userType, bpExpensesAmount)
        preparedForm   = existingAnswer.fold(form)(form.fill)
      } yield Ok(view(preparedForm, mode, request.userType, taxYear, businessId))).merge
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleForm(bpExpensesAmount: BigDecimal): Future[Result] =
        formProvider(request.userType, bpExpensesAmount)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId))),
            value => handleSuccess(value)
          )
      def handleSuccess(value: BigDecimal): Future[Result] =
        service
          .persistAnswer(businessId, request.userAnswers, value, WfbpClaimingAmountPage)
          .map(updated => Redirect(navigator.nextPage(WfbpClaimingAmountPage, mode, updated, taxYear, businessId)))

      val getExpensesAmount = request.valueOrRedirectDefault(BusinessPremisesAmountPage, businessId)

      (for {
        bpExpensesAmount <- EitherT.fromEither[Future](getExpensesAmount)
        result           <- EitherT.right[Result](handleForm(bpExpensesAmount))
      } yield result).merge
  }
}
