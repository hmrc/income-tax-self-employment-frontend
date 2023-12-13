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
import models.NormalMode
import models.common.{BusinessId, JourneyContext, TaxYear}
import models.domain.ApiResultT
import models.errors.ServiceError
import models.journeys.Journey
import play.api.Logger
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

import scala.concurrent.{ExecutionContext, Future}

package object controllers {
  private def redirectJourneyRecovery(): Result = Redirect(standard.routes.JourneyRecoveryController.onPageLoad())

  private def redirectJourneyCompletedState(taxYear: TaxYear, businessId: BusinessId, journey: Journey): Result = Redirect(
    journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, businessId, journey.toString, NormalMode)
  )

  def handleResult(result: Future[Either[ServiceError, Result]])(implicit ec: ExecutionContext, logger: Logger): Future[Result] =
    handleResultT(EitherT(result))

  def handleApiResult(result: ApiResultT[_])(implicit ec: ExecutionContext): Future[_] =
    result.value.flatMap {
      case Left(error) => Future.failed(error.httpError.internalReason.getOrElse(new RuntimeException(error.httpError.toString)))
      case Right(v)    => Future.successful(v)
    }

  private def handleResultT(resultT: EitherT[Future, ServiceError, Result])(implicit ec: ExecutionContext, logger: Logger): Future[Result] =
    resultT.leftMap { httpError =>
      logger.error(s"HttpError encountered: $httpError")
      redirectJourneyRecovery()
    }.merge

  // Redirection to journey recovery on downstream error retrieval is a temporary action until we pick up the unhappy
  // path tickets (JIRA TBA).
  def handleSubmitAnswersResult(ctx: JourneyContext, result: ApiResultT[_])(implicit ec: ExecutionContext, logger: Logger): Future[Result] = {
    val resultT = result.map(_ => redirectJourneyCompletedState(ctx.taxYear, ctx.businessId, ctx.journey))
    handleResultT(resultT)
  }
}
