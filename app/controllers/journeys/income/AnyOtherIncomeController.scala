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

package controllers.journeys.income

import controllers.actions._
import forms.income.AnyOtherIncomeFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.income.AnyOtherIncomePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.income.AnyOtherIncomeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AnyOtherIncomeController @Inject()(override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         formProvider: AnyOtherIncomeFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: AnyOtherIncomeView)
                                        (implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def isAgentString(isAgent: Boolean) = if (isAgent) "agent" else "individual"

  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData) { //TODO add requireData SASS-5841
    implicit request =>

      val isAgent = isAgentString(request.user.isAgent)
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(AnyOtherIncomePage) match {
        case None => formProvider(isAgent)
        case Some(value) => formProvider(isAgent).fill(value)
      }

      Ok(view(preparedForm, mode, isAgent, taxYear))
  }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData) async { //TODO add requireData SASS-5841
    implicit request =>

      formProvider(isAgentString(request.user.isAgent)).bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, isAgentString(request.user.isAgent), taxYear))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.userId)).set(AnyOtherIncomePage, value))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(AnyOtherIncomePage, mode, updatedAnswers, taxYear))
      )
  }
}
