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
import models.Mode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.capitalallowances.zeroEmissionCars.ZecUseOutsideSE.{DifferentAmount, Fifty, Ten, TwentyFive}
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
      val formProvider    = ZecUseOutsideSEFormProvider()
      val radioValue      = request.getValue(ZecUseOutsideSEPage, businessId)
      val percentageValue = request.getValue(ZecUseOutsideSEPercentagePage, businessId)
      val filledForm = (radioValue, percentageValue) match {
        case (Some(radioValue), Some(percentageValue)) if radioValue == DifferentAmount =>
          formProvider.fill(ZecUseOutsideSEFormModel(radioValue, Some(percentageValue)))
        case (Some(radioValue), _) =>
          formProvider.fill(ZecUseOutsideSEFormModel(radioValue, None))
        case _ => formProvider
      }

      Ok(view(filledForm, mode, request.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleSuccess(answer: ZecUseOutsideSEFormModel): Future[Result] =
        handleAnswer(answer).flatMap(updatedAnswers =>
          service
            .persistAnswer(businessId, updatedAnswers, answer.radioPercentage, ZecUseOutsideSEPage)
            .map(updatedAnswers => Redirect(navigator.nextPage(ZecUseOutsideSEPage, mode, updatedAnswers, taxYear, businessId))))

      def handleAnswer(answer: ZecUseOutsideSEFormModel): Future[UserAnswers] = {
        val percentage: Int = answer.radioPercentage match {
          case Ten             => 10
          case TwentyFive      => 25
          case Fifty           => 50
          case DifferentAmount => answer.optDifferentAmount.getOrElse(0)
        }
        for {
          updatedAnswers <- Future.fromTry(request.userAnswers.set(ZecUseOutsideSEPage, answer.radioPercentage, Some(businessId)))
          resultAnswers  <- Future.fromTry(updatedAnswers.set(ZecUseOutsideSEPercentagePage, percentage, Some(businessId)))
        } yield resultAnswers
      }

      ZecUseOutsideSEFormProvider()
        .bindFromRequest()
        .fold(
          formErrors => Future.successful(BadRequest(view(formErrors, mode, request.userType, taxYear, businessId))),
          value => {
            println("------ form " + value)
            handleSuccess(value)
          }
        )
  }

}
