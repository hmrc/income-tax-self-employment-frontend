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
import forms.capitalallowances.zeroEmissionCars.ZecUseOutsideSEFormProvider
import forms.capitalallowances.zeroEmissionCars.ZecUseOutsideSEFormProvider.ZecUseOutsideSEFormModel
import models.common.{BusinessId, TaxYear}
import models.journeys.capitalallowances.zeroEmissionCars.ZecUseOutsideSE.DifferentAmount
import models.{Mode, NormalMode}
import navigation.CapitalAllowancesNavigator
import pages.capitalallowances.zeroEmissionCars.{ZecUseOutsideSEPage, ZecUseOutsideSEPercentagePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.capitalallowances.zeroEmissionCars.ZecUseOutsideSEView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
@Singleton
class ZecUseOutsideSEController @Inject() (override val messagesApi: MessagesApi,
                                           navigator: CapitalAllowancesNavigator,
                                           identify: IdentifierAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           service: SelfEmploymentService,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: ZecUseOutsideSEView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val formProvider    = ZecUseOutsideSEFormProvider(request.userType)
      val radioValue      = request.getValue(ZecUseOutsideSEPage, businessId)
      val percentageValue = request.getValue(ZecUseOutsideSEPercentagePage, businessId)
      val filledForm = (radioValue, percentageValue) match {
        case (Some(radioValue), Some(percentageValue)) if radioValue == DifferentAmount =>
          formProvider.fill(ZecUseOutsideSEFormModel(radioValue, percentageValue))
        case (Some(radioValue), _) =>
          formProvider.fill(ZecUseOutsideSEFormModel(radioValue))
        case _ => formProvider
      }

      Ok(view(filledForm, mode, request.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleSuccess(answer: ZecUseOutsideSEFormModel): Future[Result] =
        for {
          updatedAnswers <- Future.fromTry(request.userAnswers.set(ZecUseOutsideSEPage, answer.radioPercentage, Some(businessId)))
          redirectMode = if (request.getValue(ZecUseOutsideSEPercentagePage, businessId) contains answer.optDifferentAmount) mode else NormalMode
          finalAnswers <- service.persistAnswer(businessId, updatedAnswers, answer.optDifferentAmount, ZecUseOutsideSEPercentagePage)
        } yield Redirect(navigator.nextPage(ZecUseOutsideSEPage, redirectMode, finalAnswers, taxYear, businessId))

      ZecUseOutsideSEFormProvider(request.userType)
        .bindFromRequest()
        .fold(
          formErrors => Future.successful(BadRequest(view(formErrors, mode, request.userType, taxYear, businessId))),
          value => handleSuccess(value)
        )
  }

}
