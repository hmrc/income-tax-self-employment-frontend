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

package controllers.journeys.capitalallowances.zeroEmissionCars

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.capitalallowances.zeroEmissionCars.ZecUsedForSelfEmploymentFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import navigation.CapitalAllowancesNavigator
import pages.capitalallowances.zeroEmissionCars.ZecUsedForSelfEmploymentPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.capitalallowances.zeroEmissionCars.ZecUsedForSelfEmploymentView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ZecUsedForSelfEmploymentController @Inject() (override val messagesApi: MessagesApi,
                                                    navigator: CapitalAllowancesNavigator,
                                                    identify: IdentifierAction,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    service: SelfEmploymentService,
                                                    formProvider: ZecUsedForSelfEmploymentFormProvider,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    view: ZecUsedForSelfEmploymentView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form = request.userAnswers
        .get(ZecUsedForSelfEmploymentPage, Some(businessId))
        .fold(formProvider(request.userType))(formProvider(request.userType).fill)

      Ok(view(form, mode, request.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      formProvider(request.userType)
        .bindFromRequest()
        .fold(
          formErrors => Future.successful(BadRequest(view(formErrors, mode, request.userType, taxYear, businessId))),
          value =>
            service
              .persistAnswer(businessId, request.userAnswers, value, ZecUsedForSelfEmploymentPage)
              .map(updatedAnswers => Redirect(navigator.nextPage(ZecUsedForSelfEmploymentPage, mode, updatedAnswers, taxYear, businessId)))
        )
  }

}
