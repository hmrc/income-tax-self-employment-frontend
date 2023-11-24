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

import cats.data.EitherT
import models.errors.HttpError
import play.api.Logger
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

import scala.concurrent.{ExecutionContext, Future}

package object controllers {
  def redirectJourneyRecovery(): Result = Redirect(standard.routes.JourneyRecoveryController.onPageLoad())

  def handleResult(result: Future[Either[HttpError, Result]])(implicit ec: ExecutionContext, logger: Logger): Future[Result] =
    EitherT(result).leftMap { httpError =>
      logger.error(s"HttpError encountered: $httpError")
      redirectJourneyRecovery()
    }.merge

}
