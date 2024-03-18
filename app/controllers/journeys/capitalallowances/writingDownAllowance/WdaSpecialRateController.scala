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

package controllers.journeys.capitalallowances.writingDownAllowance

import cats.implicits.catsSyntaxOptionId
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.capitalallowances.writingDownAllowance.WdaSpecialRateFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import pages.capitalallowances.writingDownAllowance.WdaSpecialRatePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.capitalallowances.writingDownAllowance.WdaSpecialRateView

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class WdaSpecialRateController @Inject() (override val messagesApi: MessagesApi,
                                          val controllerComponents: MessagesControllerComponents,
                                          identify: IdentifierAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: WdaSpecialRateFormProvider,
                                          service: SelfEmploymentService,
                                          view: WdaSpecialRateView)
    extends FrontendBaseController
    with I18nSupport
    with Logging {
  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form = request.userAnswers
        .get(WdaSpecialRatePage, businessId.some)
        .fold(formProvider(request.userType))(formProvider(request.userType).fill)
      Ok(view(form, mode, request.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      formProvider(request.userType)
        .bindFromRequest()
        .fold(
          formErrors => Future.successful(BadRequest(view(formErrors, mode, request.userType, taxYear, businessId))),
          answer => service.submitAnswerAndRedirect(WdaSpecialRatePage, businessId, request, answer, taxYear, mode)
        )
  }
}
