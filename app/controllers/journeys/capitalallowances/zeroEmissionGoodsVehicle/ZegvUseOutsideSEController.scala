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

package controllers.journeys.capitalallowances.zeroEmissionGoodsVehicle

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.capitalallowances.zeroEmissionGoodsVehicle.ZegvUseOutsideSEFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import pages.capitalallowances.zeroEmissionGoodsVehicle.ZegvUseOutsideSEPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvUseOutsideSEService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvUseOutsideSEViewModel
import views.html.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvUseOutsideSEView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ZegvUseOutsideSEController @Inject() (override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            service: ZegvUseOutsideSEService,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: ZegvUseOutsideSEView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val filledForm = ZegvUseOutsideSEViewModel.createFilledForm(request, businessId)
      Ok(view(filledForm, mode, request.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      ZegvUseOutsideSEFormProvider(request.userType)
        .bindFromRequest()
        .fold(
          formErrors => Future.successful(BadRequest(view(formErrors, mode, request.userType, taxYear, businessId))),
          answer =>
            service.submitAnswer(request, answer, businessId).map { updatedAnswers =>
              ZegvUseOutsideSEPage.redirectNext(mode, updatedAnswers, businessId, taxYear)
            }
        )
  }

}
