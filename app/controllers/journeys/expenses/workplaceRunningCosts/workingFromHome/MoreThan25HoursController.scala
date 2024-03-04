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
import forms.expenses.workplaceRunningCosts.workingFromHome.MoreThan25HoursFormProvider
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.requests.DataRequest
import models.{Mode, NormalMode}
import navigation.WorkplaceRunningCostsNavigator
import pages.expenses.workplaceRunningCosts.workingFromHome._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.Settable
import services.SelfEmploymentService
import services.SelfEmploymentService.clearDataFromUserAnswers
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.workplaceRunningCosts.workingFromHome.MoreThan25HoursView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MoreThan25HoursController @Inject() (override val messagesApi: MessagesApi,
                                           service: SelfEmploymentService,
                                           navigator: WorkplaceRunningCostsNavigator,
                                           identify: IdentifierAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: MoreThan25HoursFormProvider,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: MoreThan25HoursView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form = request.userAnswers
        .get(MoreThan25HoursPage, Some(businessId))
        .fold(formProvider(request.userType))(formProvider(request.userType).fill)

      Ok(view(form, mode, request.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleSuccess(answer: Boolean): Future[Result] =
        for {
          (editedUserAnswers, redirectMode) <- handleGatewayQuestion(answer, request, mode, businessId)
          updatedUserAnswers                <- service.persistAnswer(businessId, editedUserAnswers, answer, MoreThan25HoursPage)
        } yield Redirect(navigator.nextPage(MoreThan25HoursPage, redirectMode, updatedUserAnswers, taxYear, businessId))

      formProvider(request.userType)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId))),
          value => handleSuccess(value)
        )
  }

  private def handleGatewayQuestion(currentAnswer: Boolean,
                                    request: DataRequest[_],
                                    mode: Mode,
                                    businessId: BusinessId): Future[(UserAnswers, Mode)] = {
    val pagesToBeCleared: List[Settable[_]] =
      List(WorkingFromHomeHours25To50, WorkingFromHomeHours51To100, WorkingFromHomeHours101Plus, WfhFlatRateOrActualCostsPage)
    val clearUserAnswerDataIfNeeded = if (currentAnswer) {
      Future(request.userAnswers)
    } else {
      Future.fromTry(clearDataFromUserAnswers(request.userAnswers, pagesToBeCleared, Some(businessId)))
    }
    val redirectMode = request.getValue(MoreThan25HoursPage, businessId) match {
      case Some(false) if currentAnswer => NormalMode
      case _                            => mode
    }
    clearUserAnswerDataIfNeeded.map(editedUserAnswers => (editedUserAnswers, redirectMode))
  }
}
