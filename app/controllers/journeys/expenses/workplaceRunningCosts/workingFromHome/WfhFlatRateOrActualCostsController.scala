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
import models.requests.DataRequest
import models.{Mode, NormalMode}
import navigation.ExpensesNavigator
import pages.expenses.workplaceRunningCosts.workingFromHome.WfhFlatRateOrActualCostsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.workplaceRunningCosts.workingFromHome.WfhFlatRateOrActualCostsView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class WfhFlatRateOrActualCostsController @Inject()(override val messagesApi: MessagesApi,
                                                   service: SelfEmploymentService,
                                                   navigator: ExpensesNavigator,
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
      val flatRateAmount = calculateFlatRateAmount(request, businessId)
      val form = request
        .getValue(WfhFlatRateOrActualCostsPage, businessId)
        .fold(formProvider(request.userType))(formProvider(request.userType).fill)

      Ok(view(form, mode, request.userType, taxYear, businessId, flatRateAmount))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleSuccess(userAnswers: UserAnswers, answer: WfhFlatRateOrActualCosts): Future[Result] = {
        for {
          result <- service
            .persistAnswer(businessId, userAnswers, answer, WfhFlatRateOrActualCostsPage)
            .map(updated => Redirect(navigator.nextPage(WfhFlatRateOrActualCostsPage, mode, updated, taxYear, businessId)))
        } yield result
      }

      val flatRateAmount = calculateFlatRateAmount(request, businessId)
      formProvider(request.userType)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, flatRateAmount))),
          value => handleSuccess(request.userAnswers, value)
        )
  }

  private def calculateFlatRateAmount(request: DataRequest[_], businessId: BusinessId): String = {
//    request.getValue(WorkFromHomeHoursPage, businessId)
  "100.00"
  }

}
