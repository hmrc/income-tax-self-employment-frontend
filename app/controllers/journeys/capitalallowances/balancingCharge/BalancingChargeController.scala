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

package controllers.journeys.capitalallowances.balancingCharge

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.journeys.fillForm
import forms.standard.BooleanFormProvider
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.requests.DataRequest
import models.{Mode, NormalMode}
import navigation.CapitalAllowancesNavigator
import pages.capitalallowances.balancingCharge.{BalancingChargeAmountPage, BalancingChargePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.Settable
import services.SelfEmploymentService
import services.SelfEmploymentService.clearDataFromUserAnswers
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.capitalallowances.balancingCharge.BalancingChargeView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BalancingChargeController @Inject() (override val messagesApi: MessagesApi,
                                           navigator: CapitalAllowancesNavigator,
                                           identify: IdentifierAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           service: SelfEmploymentService,
                                           formProvider: BooleanFormProvider,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: BalancingChargeView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val page = BalancingChargePage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form = fillForm(page, businessId, formProvider(page, request.userType))
      Ok(view(form, mode, request.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      formProvider(page, request.userType)
        .bindFromRequest()
        .fold(
          formErrors => Future.successful(BadRequest(view(formErrors, mode, request.userType, taxYear, businessId))),
          answer =>
            for {
              (editedUserAnswers, redirectMode) <- handleGatewayQuestion(answer, request, mode, businessId)
              updatedUserAnswers                <- service.persistAnswer(businessId, editedUserAnswers, answer, BalancingChargePage)
            } yield Redirect(navigator.nextPage(BalancingChargePage, redirectMode, updatedUserAnswers, taxYear, businessId))
        )
  }

  private def handleGatewayQuestion(currentAnswer: Boolean,
                                    request: DataRequest[_],
                                    mode: Mode,
                                    businessId: BusinessId): Future[(UserAnswers, Mode)] = {
    val pagesToBeCleared: List[Settable[_]] = List(BalancingChargeAmountPage)
    val clearUserAnswerDataIfNeeded = if (currentAnswer) {
      Future(request.userAnswers)
    } else {
      Future.fromTry(clearDataFromUserAnswers(request.userAnswers, pagesToBeCleared, Some(businessId)))
    }
    val redirectMode = request.getValue(BalancingChargePage, businessId) match {
      case Some(false) if currentAnswer => NormalMode
      case _                            => mode
    }
    clearUserAnswerDataIfNeeded.map(editedUserAnswers => (editedUserAnswers, redirectMode))
  }

}
