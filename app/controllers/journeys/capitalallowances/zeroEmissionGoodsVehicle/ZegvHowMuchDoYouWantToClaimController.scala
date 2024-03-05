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
import forms.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaimFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import pages.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaimPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaimService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaimViewModel
import views.html.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaimView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ZegvHowMuchDoYouWantToClaimController @Inject() (override val messagesApi: MessagesApi,
                                                       service: ZegvHowMuchDoYouWantToClaimService,
                                                       identify: IdentifierAction,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       val controllerComponents: MessagesControllerComponents,
                                                       view: ZegvHowMuchDoYouWantToClaimView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val maybeFilledForm = ZegvHowMuchDoYouWantToClaimViewModel.createFillForm(request, businessId)
      maybeFilledForm.fold(ZegvHowMuchDoYouWantToClaimPage.redirectToRecoveryPage) { case (filledForm, fullCost) =>
        Ok(view(filledForm, mode, request.userType, taxYear, businessId, fullCost))
      }
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      val calculatedCost = ZegvHowMuchDoYouWantToClaimViewModel.calcFullCost(request, businessId)
      calculatedCost.fold(Future(ZegvHowMuchDoYouWantToClaimPage.redirectToRecoveryPage)) { fullCost =>
        ZegvHowMuchDoYouWantToClaimFormProvider(request.userType, fullCost)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, fullCost))),
            value =>
              service.submitAnswer(request.userAnswers, value, fullCost, businessId).map { updatedAnswer =>
                ZegvHowMuchDoYouWantToClaimPage.redirectNext(mode, updatedAnswer, businessId, taxYear)
              }
          )
      }
  }

}
