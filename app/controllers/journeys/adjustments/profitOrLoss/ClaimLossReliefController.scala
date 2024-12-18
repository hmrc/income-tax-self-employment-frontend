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

package controllers.journeys.adjustments.profitOrLoss

import controllers.actions._
import controllers.journeys.fillForm
import forms.standard.BooleanFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import pages.adjustments.profitOrLoss.ClaimLossReliefPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.adjustments.profitOrLoss.ClaimLossReliefView

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class ClaimLossReliefController @Inject() (override val messagesApi: MessagesApi,
                                           val controllerComponents: MessagesControllerComponents,
                                           service: SelfEmploymentService,
                                           identify: IdentifierAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: BooleanFormProvider,
                                           view: ClaimLossReliefView)
    extends FrontendBaseController
    with I18nSupport {

  private val page = ClaimLossReliefPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val filledForm = fillForm(page, businessId, formProvider(page, request.userType))
      Ok(view(filledForm, taxYear, businessId, request.userType, mode))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleError(formWithErrors: Form[_]): Result = BadRequest(view(formWithErrors, taxYear, businessId, request.userType, mode))

      def handleSuccess(answer: Boolean): Future[Result] =
        service.submitGatewayQuestionAndRedirect(page, businessId, request.userAnswers, answer, taxYear, mode)

      service.handleForm(formProvider(page, request.userType), handleError, handleSuccess)
  }

}
