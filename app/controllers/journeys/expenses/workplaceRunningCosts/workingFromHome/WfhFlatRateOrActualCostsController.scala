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

package controllers.journeys.expenses.workplaceRunningCosts.workingFromHome

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.expenses.workplaceRunningCosts.workingFromHome.WfhFlatRateOrActualCostsFormProvider
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.expenses.workplaceRunningCosts.WfhFlatRateOrActualCosts
import models.journeys.expenses.workplaceRunningCosts.WfhFlatRateOrActualCosts.{ActualCosts, FlatRate}
import models.requests.DataRequest
import models.{Mode, NormalMode}
import navigation.WorkplaceRunningCostsNavigator
import pages.expenses.workplaceRunningCosts.workingFromHome.{WfhClaimingAmountPage, WfhFlatRateOrActualCostsPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.expenses.workplaceRunningCosts.WfhFlatRateViewModel.calculateFlatRate
import views.html.journeys.expenses.workplaceRunningCosts.workingFromHome.WfhFlatRateOrActualCostsView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WfhFlatRateOrActualCostsController @Inject() (override val messagesApi: MessagesApi,
                                                    service: SelfEmploymentService,
                                                    navigator: WorkplaceRunningCostsNavigator,
                                                    identify: IdentifierAction,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    formProvider: WfhFlatRateOrActualCostsFormProvider,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    view: WfhFlatRateOrActualCostsView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      calculateFlatRate(request, businessId) match {
        case Left(redirect) => redirect
        case Right(flatRateViewModel) =>
          val form = request
            .getValue(WfhFlatRateOrActualCostsPage, businessId)
            .fold(formProvider(request.userType))(formProvider(request.userType).fill)

          Ok(view(form, mode, request.userType, taxYear, businessId, flatRateViewModel))
      }
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleSuccess(answer: WfhFlatRateOrActualCosts, flatRate: BigDecimal): Future[Result] =
        for {
          (editedUserAnswers, redirectMode) <- handleGatewayQuestion(answer, flatRate, request, mode, businessId)
          updatedUserAnswers                <- service.persistAnswer(businessId, editedUserAnswers, answer, WfhFlatRateOrActualCostsPage)
        } yield Redirect(navigator.nextPage(WfhFlatRateOrActualCostsPage, redirectMode, updatedUserAnswers, taxYear, businessId))

      calculateFlatRate(request, businessId) match {
        case Left(redirect) => Future.successful(redirect)
        case Right(flatRateViewModel) =>
          formProvider(request.userType)
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, flatRateViewModel))),
              answer => handleSuccess(answer, BigDecimal(flatRateViewModel.flatRate))
            )
      }
  }

  private def handleGatewayQuestion(currentAnswer: WfhFlatRateOrActualCosts,
                                    flatRate: BigDecimal,
                                    request: DataRequest[_],
                                    mode: Mode,
                                    businessId: BusinessId): Future[(UserAnswers, Mode)] = {
    val clearUserAnswerDataIfNeeded = currentAnswer match {
      case FlatRate    => Future.fromTry(request.userAnswers.set(WfhClaimingAmountPage, flatRate, Some(businessId)))
      case ActualCosts => Future(request.userAnswers)
    }
    val redirectMode = request.getValue(WfhFlatRateOrActualCostsPage, businessId) match {
      case Some(FlatRate) if currentAnswer == ActualCosts => NormalMode
      case _                                              => mode
    }
    clearUserAnswerDataIfNeeded.map(editedUserAnswers => (editedUserAnswers, redirectMode))
  }

}
