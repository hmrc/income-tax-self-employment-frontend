/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.journeys.expenses

import controllers.actions._
import forms.expenses.DisallowableInterestFormProvider
import models.{Mode, UserAnswers}
import navigation.ExpensesNavigator
import pages.expenses.DisallowableInterestPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.DisallowableInterestView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DisallowableInterestController @Inject() (override val messagesApi: MessagesApi,
                                                sessionRepository: SessionRepository,
                                                navigator: ExpensesNavigator,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: DisallowableInterestFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: DisallowableInterestView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val businessId = "SJPR05893938418"
  val taxYear    = LocalDate.now.getYear

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData) { implicit request =>
    val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(DisallowableInterestPage) match {
      case None        => formProvider()
      case Some(value) => formProvider().fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData) async { implicit request =>
    formProvider()
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.userId)).set(DisallowableInterestPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(DisallowableInterestPage, mode, updatedAnswers))
      )
  }

}
