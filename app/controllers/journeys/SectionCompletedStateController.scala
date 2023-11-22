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

package controllers.journeys

import connectors.SelfEmploymentConnector
import controllers.actions._
import controllers.standard.routes.JourneyRecoveryController
import forms.SectionCompletedStateFormProvider
import models.Mode
import models.journeys.CompletedSectionState
import models.journeys.CompletedSectionState.{No, Yes}
import navigation.GeneralNavigator
import pages.SectionCompletedStatePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.SectionCompletedStateView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SectionCompletedStateController @Inject() (override val messagesApi: MessagesApi,
                                                 selfEmploymentConnector: SelfEmploymentConnector,
                                                 navigator: GeneralNavigator,
                                                 identify: IdentifierAction,
                                                 getData: DataRetrievalAction,
                                                 formProvider: SectionCompletedStateFormProvider,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 view: SectionCompletedStateView)(implicit val ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[CompletedSectionState] = formProvider()

  def onPageLoad(taxYear: Int, businessId: String, journey: String, mode: Mode): Action[AnyContent] = (identify andThen getData) async {
    implicit request =>
      val preparedForm = selfEmploymentConnector.getJourneyState(businessId, journey, taxYear, request.user.mtditid) map {
        case Right(Some(true))  => form.fill(Yes)
        case Right(Some(false)) => form.fill(No)
        case Right(None)        => form
        case Left(_)            => form
      }

      preparedForm map { form =>
        Ok(view(form, taxYear, businessId, journey, mode))
      }
  }

  def onSubmit(taxYear: Int, businessId: String, journey: String, mode: Mode): Action[AnyContent] = (identify andThen getData) async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, taxYear, businessId, journey, mode))),
          value =>
            selfEmploymentConnector.saveJourneyState(businessId, journey, taxYear, complete = value.equals(Yes), request.user.mtditid) map {
              case Right(_) => Redirect(navigator.nextPage(SectionCompletedStatePage, taxYear))
              case _        => Redirect(JourneyRecoveryController.onPageLoad())
            }
        )
  }

}
