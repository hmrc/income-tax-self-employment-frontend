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

package controllers.standard

import config.FrontendAppConfig
import controllers.actions.IdentifierAction
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl._
import uk.gov.hmrc.play.bootstrap.binders._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.standard.{JourneyRecoveryContinueView, JourneyRecoveryStartAgainView}

import javax.inject.Inject

class JourneyRecoveryController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    identify: IdentifierAction,
    continueView: JourneyRecoveryContinueView,
    startAgainView: JourneyRecoveryStartAgainView
)(implicit val appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(continueUrl: Option[RedirectUrl] = None): Action[AnyContent] = identify { implicit request =>
    val errorDescription: Option[String] = request2flash.get("errorMessage")
    val safeUrl: Option[String] = continueUrl.flatMap { unsafeUrl =>
      unsafeUrl.getEither(OnlyRelative) match {
        case Right(safeUrl) =>
          Some(safeUrl.url)
        case Left(message) =>
          logger.info(message)
          None
      }
    }

    safeUrl
      .map(url => Ok(continueView(errorDescription, url)))
      .getOrElse(Ok(startAgainView(errorDescription)))
  }
}
