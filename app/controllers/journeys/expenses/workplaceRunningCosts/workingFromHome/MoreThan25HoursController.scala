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
import models.journeys.expenses.workplaceRunningCosts.workingFromHome.MoreThan25Hours
import models.journeys.expenses.workplaceRunningCosts.workingFromHome.MoreThan25Hours._
import models.{Mode, NormalMode}
import navigation.ExpensesNavigator
import pages.expenses.workplaceRunningCosts.workingFromHome._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.Settable
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.workplaceRunningCosts.workingFromHome.MoreThan25HoursView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class MoreThan25HoursController @Inject() (override val messagesApi: MessagesApi,
                                           service: SelfEmploymentService,
                                           navigator: ExpensesNavigator,
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
      def handleSuccess(userAnswers: UserAnswers, answer: MoreThan25Hours): Future[Result] = {
        val redirectMode = continueAsNormalModeIfPrevAnswerChanged(answer)
        for {
          editedUserAnswers <- clearDataFromUserAnswers(userAnswers, answer, businessId)
          result <- service
            .persistAnswer(businessId, editedUserAnswers, answer, MoreThan25HoursPage)
            .map(updated => Redirect(navigator.nextPage(MoreThan25HoursPage, redirectMode, updated, taxYear, businessId)))
        } yield result
      }
      def continueAsNormalModeIfPrevAnswerChanged(currentAnswer: MoreThan25Hours): Mode =
        request.getValue(MoreThan25HoursPage, businessId) match {
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

  private def clearDataFromUserAnswers(userAnswers: UserAnswers, pageAnswer: MoreThan25Hours, businessId: BusinessId): Future[UserAnswers] =
    if (pageAnswer == No) {
      def removePageData(page: Settable[_], userAnswers: UserAnswers): Try[UserAnswers] =
        userAnswers.remove(page, Some(businessId)) match {
          case Success(updatedUserAnswers) => Try(updatedUserAnswers)
          case Failure(exception)          => Failure(exception)
        }

      for {
        update1 <- Future.fromTry(removePageData(WorkingFromHomeHours25To50, userAnswers))
        update2 <- Future.fromTry(removePageData(WorkingFromHomeHours51To100, update1))
        update3 <- Future.fromTry(removePageData(WorkingFromHomeHours101Plus, update2))
        result  <- Future.fromTry(removePageData(WfhFlatRateOrActualCostsPage, update3))
      } yield result
    } else {
      Future(userAnswers)
    }
}
