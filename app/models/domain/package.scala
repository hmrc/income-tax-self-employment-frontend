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

package models

import cats.data.EitherT
import controllers.standard
import models.errors.ServiceError
import play.api.Logger
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

import scala.concurrent.{ExecutionContext, Future}

package object domain {
  type ApiResultT[A] = EitherT[Future, ServiceError, A]

  implicit class ApiResultOps[A](underlying: ApiResultT[A]) {

    /** Helper method to handle HttpError which should be logged and recovered by redirecting to the journey recovery page
      */
    def result(implicit ec: ExecutionContext, logger: Logger): EitherT[Future, Result, A] =
      underlying.leftMap { httpError =>
        logger.error(s"HttpError encountered: $httpError")
        Redirect(standard.routes.JourneyRecoveryController.onPageLoad())
      }
  }

}
