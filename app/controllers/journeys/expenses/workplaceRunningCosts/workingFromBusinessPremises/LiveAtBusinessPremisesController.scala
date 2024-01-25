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

import controllers.actions._
import forms.expenses.workplaceRunningCosts.workingFromBusinessPremises.LiveAtBusinessPremisesFormProvider
import models.{Mode, NormalMode}
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.expenses.workplaceRunningCosts.LiveAtBusinessPremises
import models.journeys.expenses.workplaceRunningCosts.LiveAtBusinessPremises.{No, Yes}
import navigation.ExpensesNavigator
import pages.expenses.workplaceRunningCosts.workingFromBusinessPremises.LiveAtBusinessPremisesPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.workplaceRunningCosts.workingFromBusinessPremises.LiveAtBusinessPremisesView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class LiveAtBusinessPremisesController @Inject() (override val messagesApi: MessagesApi,
                                                  selfEmploymentService: SelfEmploymentService,
                                                  navigator: ExpensesNavigator,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  formProvider: LiveAtBusinessPremisesFormProvider,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: LiveAtBusinessPremisesView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(LiveAtBusinessPremisesPage, Some(businessId)) match {
        case None        => formProvider(request.userType)
        case Some(value) => formProvider(request.userType).fill(value)
      }

      Ok(view(preparedForm, mode, request.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleSuccess(userAnswers: UserAnswers, answer: LiveAtBusinessPremises): Future[Result] = {
        val redirectMode = continueAsNormalModeIfPrevAnswerChanged(answer)
        for {
          editedUserAnswers <- Future.fromTry(clearDataFromUserAnswers(userAnswers, answer))
          result <- selfEmploymentService
            .persistAnswer(businessId, editedUserAnswers, answer, LiveAtBusinessPremisesPage)
            .map(updated => Redirect(navigator.nextPage(LiveAtBusinessPremisesPage, redirectMode, updated, taxYear, businessId)))
        } yield result
      }

      def continueAsNormalModeIfPrevAnswerChanged(currentAnswer: LiveAtBusinessPremises): Mode =
        request.getValue(LiveAtBusinessPremisesPage, businessId) match {
          case Some(No) if currentAnswer == Yes => NormalMode
          case _                                => mode
        }

      formProvider(request.userType)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId))),
          value => handleSuccess(request.userAnswers, value)
        )
  }

  private def clearDataFromUserAnswers(userAnswers: UserAnswers, pageAnswer: LiveAtBusinessPremises): Try[UserAnswers] =
    if (pageAnswer == No) {
      // TODO add removePageData for months someone lived at your business premises page and claim personal use amount as a flat rate of £ or actual costs page
      Try(userAnswers)
    } else {
      Try(userAnswers)
    }

}