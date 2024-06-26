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

package controllers.journeys.expenses.goodsToSellOrUse

import controllers.actions._
import controllers.journeys.fillForm
import forms.standard.BooleanFormProvider
import models.common.{BusinessId, TaxYear}
import models.{Mode, NormalMode}
import pages.expenses.goodsToSellOrUse.TaxiMinicabOrRoadHaulagePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.tailoring.individualCategories.TaxiMinicabOrRoadHaulageView

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class TaxiMinicabOrRoadHaulageController @Inject() (override val messagesApi: MessagesApi,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    service: SelfEmploymentService,
                                                    identify: IdentifierAction,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    formProvider: BooleanFormProvider,
                                                    view: TaxiMinicabOrRoadHaulageView)
    extends FrontendBaseController
    with I18nSupport {

  private val page = TaxiMinicabOrRoadHaulagePage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form = fillForm(page, businessId, formProvider(page, request.userType))

      Ok(view(form, mode, request.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleError(formWithErrors: Form[_]): Result = BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId))
      def handleSuccess(answer: Boolean): Future[Result] = {
        val normalModeIfAnswerChanged =
          if (request.getValue(TaxiMinicabOrRoadHaulagePage, businessId).contains(!answer)) NormalMode else mode
        service.submitGatewayQuestionAndRedirect(page, businessId, request.userAnswers, answer, taxYear, normalModeIfAnswerChanged)
      }

      service.handleForm(formProvider(page, request.userType), handleError, handleSuccess)
  }

}
