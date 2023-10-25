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

package controllers.journeys.abroad

import controllers.actions._
import controllers.standard.routes.JourneyRecoveryController
import forms.abroad.SelfEmploymentAbroadFormProvider
import models.{Mode, UserAnswers}
import navigation.AbroadNavigator
import pages.abroad.SelfEmploymentAbroadPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.abroad.SelfEmploymentAbroadView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelfEmploymentAbroadController @Inject() (override val messagesApi: MessagesApi,
                                                sessionRepository: SessionRepository,
                                                navigator: AbroadNavigator,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                formProvider: SelfEmploymentAbroadFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: SelfEmploymentAbroadView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: Int, businessId: String, mode: Mode): Action[AnyContent] = (identify andThen getData) { implicit request =>
    val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(SelfEmploymentAbroadPage, Some(businessId)) match {
      case None        => formProvider(request.user.isAgent)
      case Some(value) => formProvider(request.user.isAgent).fill(value)
    }

    Ok(view(preparedForm, taxYear, businessId, request.user.isAgent, mode))
  }

  def onSubmit(taxYear: Int, businessId: String, mode: Mode): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    formProvider(request.user.isAgent)
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, taxYear, businessId, request.user.isAgent, mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(
              request.userAnswers.getOrElse(UserAnswers(request.userId)).set(SelfEmploymentAbroadPage, value, Some(businessId)))
            isSuccessful <- sessionRepository.set(updatedAnswers)
          } yield {
            val redirectLocation =
              if (isSuccessful) navigator.nextPage(SelfEmploymentAbroadPage, mode, updatedAnswers, taxYear, businessId)
              else JourneyRecoveryController.onPageLoad()
            Redirect(redirectLocation)
          }
      )
  }

}
