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

package controllers.testonly

import cats.data.EitherT
import connectors.SelfEmploymentConnector
import models.errors.ServiceError
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TestManagerController @Inject() (
    sessionRepository: SessionRepository,
    selfEmploymentConnector: SelfEmploymentConnector,
    val controllerComponents: MessagesControllerComponents
) extends FrontendBaseController
    with Logging {

  def isTestEnabled: Action[AnyContent] = Action {
    Ok("true")
  }

  def clearAllData(): Action[AnyContent] = Action.async {
    implicit val ec = ExecutionContext.global

    (for {
      _ <- EitherT.right[ServiceError](sessionRepository.testOnlyClearAllData())
      _ <- selfEmploymentConnector.clearDatabase()(HeaderCarrier(), ec)
    } yield NoContent).fold(
      error => InternalServerError(s"Error clearing data: $error"),
      _ => NoContent
    )
  }
}
