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

package controllers

import controllers.actions._
import forms.SelfEmploymentAbroadFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.SelfEmploymentAbroadPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SelfEmploymentAbroadView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelfEmploymentAbroadController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                sessionRepository: SessionRepository,
                                                navigator: Navigator,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                formProvider: SelfEmploymentAbroadFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: SelfEmploymentAbroadView
                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, nino: String, mode: Mode): Action[AnyContent] = (identify andThen getData) {
    implicit request =>

      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(SelfEmploymentAbroadPage) match {
        case None => formProvider() //ToDo give 'isAgent' argument to formProvider when 'user.isAgent' is created + edit in view
        case Some(value) => formProvider().fill(value)
      }

      Ok(view(preparedForm, taxYear, nino, mode))
  }

  def onSubmit(taxYear: Int, nino: String, mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>

      formProvider().bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, taxYear, nino, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.userId)).set(SelfEmploymentAbroadPage, value))
            isSuccessful <- sessionRepository.set(updatedAnswers)
          } yield {
            val redirectLocation =
              if (isSuccessful) navigator.nextPage(SelfEmploymentAbroadPage, mode, updatedAnswers)
              else routes.JourneyRecoveryController.onPageLoad()
            Redirect(redirectLocation)
          }
      )
  }
}
