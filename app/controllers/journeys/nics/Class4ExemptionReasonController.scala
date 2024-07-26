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

package controllers.journeys.nics

import controllers.actions._
import controllers.journeys.fillForm
import forms.standard.EnumerableFormProvider
import models.Mode
import models.common.BusinessId.nationalInsuranceContributions
import models.common.TaxYear
import models.journeys.nics.ExemptionReason
import pages.nics.Class4ExemptionReasonPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.nics.Class4ExemptionReasonView

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class Class4ExemptionReasonController @Inject() (override val messagesApi: MessagesApi,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 identify: IdentifierAction,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 formProvider: EnumerableFormProvider,
                                                 service: SelfEmploymentService,
                                                 view: Class4ExemptionReasonView)
    extends FrontendBaseController
    with I18nSupport {

  private val page = Class4ExemptionReasonPage

  def onPageLoad(taxYear: TaxYear, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val existingAnswer = request.getValue(page, nationalInsuranceContributions)
    val form           = fillForm(page, nationalInsuranceContributions, formProvider(page, request.userType))
    val preparedForm   = existingAnswer.fold(form)(form.fill)
    Ok(view(preparedForm, taxYear, request.userType, mode))
  }

  def onSubmit(taxYear: TaxYear, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async { implicit request =>
    def handleError(formWithErrors: Form[_]): Result = BadRequest(view(formWithErrors, taxYear, request.userType, mode))
    def handleSuccess(answer: ExemptionReason): Future[Result] =
      service.submitGatewayQuestionAndRedirect(page, nationalInsuranceContributions, request.userAnswers, answer, taxYear, mode)

    service.handleForm(formProvider(page, request.userType), handleError, handleSuccess)
  }
}
