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

package controllers.journeys.capitalallowances.electricVehicleChargePoints

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.capitalallowances.electricVehicleChargePoints.EvcpUseOutsideSEFormProvider
import forms.capitalallowances.electricVehicleChargePoints.EvcpUseOutsideSEFormProvider.EvcpUseOutsideSEFormModel
import models.common.{BusinessId, TaxYear}
import models.journeys.capitalallowances.electricVehicleChargePoints.EvcpUseOutsideSE.DifferentAmount
import models.{Mode, NormalMode}
import navigation.CapitalAllowancesNavigator
import pages.capitalallowances.electricVehicleChargePoints.{EvcpUseOutsideSEPage, EvcpUseOutsideSEPercentagePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.capitalallowances.electricVehicleChargePoints.EvcpUseOutsideSEView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EvcpUseOutsideSEController @Inject() (override val messagesApi: MessagesApi,
                                            navigator: CapitalAllowancesNavigator,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            service: SelfEmploymentService,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: EvcpUseOutsideSEView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val formProvider    = EvcpUseOutsideSEFormProvider(request.userType)
      val radioValue      = request.getValue(EvcpUseOutsideSEPage, businessId)
      val percentageValue = request.getValue(EvcpUseOutsideSEPercentagePage, businessId)
      val filledForm = (radioValue, percentageValue) match {
        case (Some(radioValue), Some(percentageValue)) if radioValue == DifferentAmount =>
          formProvider.fill(EvcpUseOutsideSEFormModel(radioValue, percentageValue))
        case (Some(radioValue), _) =>
          formProvider.fill(EvcpUseOutsideSEFormModel(radioValue))
        case _ => formProvider
      }

      Ok(view(filledForm, mode, request.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleSuccess(answer: EvcpUseOutsideSEFormModel): Future[Result] =
        for {
          updatedAnswers <- Future.fromTry(request.userAnswers.set(EvcpUseOutsideSEPage, answer.radioPercentage, Some(businessId)))
          redirectMode = if (request.getValue(EvcpUseOutsideSEPercentagePage, businessId) contains answer.optDifferentAmount) mode else NormalMode
          finalAnswers <- service.persistAnswer(businessId, updatedAnswers, answer.optDifferentAmount, EvcpUseOutsideSEPercentagePage)
        } yield Redirect(navigator.nextPage(EvcpUseOutsideSEPage, redirectMode, finalAnswers, taxYear, businessId))

      EvcpUseOutsideSEFormProvider(request.userType)
        .bindFromRequest()
        .fold(
          formErrors => Future.successful(BadRequest(view(formErrors, mode, request.userType, taxYear, businessId))),
          value => handleSuccess(value)
        )
  }

}
