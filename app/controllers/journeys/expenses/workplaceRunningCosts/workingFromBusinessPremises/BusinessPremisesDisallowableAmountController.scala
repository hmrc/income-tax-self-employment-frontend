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

package controllers.journeys.expenses.workplaceRunningCosts.workingFromBusinessPremises

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.standard.routes
import forms.expenses.workplaceRunningCosts.workingFromBusinessPremises.BusinessPremisesDisallowableAmountFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import navigation.WorkplaceRunningCostsNavigator
import pages.expenses.workplaceRunningCosts.workingFromBusinessPremises.{BusinessPremisesAmountPage, BusinessPremisesDisallowableAmountPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.MoneyUtils.formatMoney
import views.html.journeys.expenses.workplaceRunningCosts.workingFromBusinessPremises.BusinessPremisesDisallowableAmountView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessPremisesDisallowableAmountController @Inject() (override val messagesApi: MessagesApi,
                                                              service: SelfEmploymentService,
                                                              navigator: WorkplaceRunningCostsNavigator,
                                                              identify: IdentifierAction,
                                                              getData: DataRetrievalAction,
                                                              requireData: DataRequiredAction,
                                                              formProvider: BusinessPremisesDisallowableAmountFormProvider,
                                                              val controllerComponents: MessagesControllerComponents,
                                                              view: BusinessPremisesDisallowableAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      request.userAnswers.get(BusinessPremisesAmountPage, Some(businessId)) match {
        case None => Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        case Some(disallowableAmount) =>
          val form =
            request.userAnswers.get(BusinessPremisesDisallowableAmountPage, Some(businessId)) match {
              case None => formProvider(request.userType, disallowableAmount)
              case Some(value) => formProvider(request.userType, disallowableAmount).fill(value)
          }
          Future.successful(Ok(view(form, mode, request.userType, taxYear, businessId, formatMoney(disallowableAmount))))


      }
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      request.userAnswers.get(BusinessPremisesAmountPage, Some(businessId)) match {
        case None => Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        case Some(disallowableAmount) =>
          def handleSuccess(userAnswers: UserAnswers, value: BigDecimal): Future[Result] =
            service
              .persistAnswer(businessId, userAnswers, value, BusinessPremisesDisallowableAmountPage)
              .map(updated => Redirect(navigator.nextPage(BusinessPremisesDisallowableAmountPage, mode, updated, taxYear, businessId)))

          formProvider(request.userType, disallowableAmount)
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, formatMoney(disallowableAmount)))),
              value => handleSuccess(request.userAnswers, value)
            )
      }
  }
}
