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
import cats.implicits.catsStdInstancesForFuture
import models.NormalMode
import models.common.{AccountingType, BusinessId, JourneyContext, TaxYear}
import models.domain.{ApiResultT, BusinessData}
import models.errors.ServiceError
import models.journeys.Journey
import models.requests.DataRequest
import play.api.Logger
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import utils.Logging

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import scala.concurrent.{ExecutionContext, Future}

package object controllers extends Logging {
  def redirectJourneyRecovery(): Result = Redirect(standard.routes.JourneyRecoveryController.onPageLoad())

  private def redirectJourneyCompletedState(taxYear: TaxYear, businessId: BusinessId, journey: Journey): Result = Redirect(
    journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, businessId, journey.entryName, NormalMode)
  )

  def returnAccountingType(businessId: BusinessId)(implicit request: DataRequest[_]): AccountingType =
    request.userAnswers.getAccountingType(businessId)

  def handleApiResult[A](result: ApiResultT[A])(implicit ec: ExecutionContext): Future[A] =
    result.value.flatMap {
      case Left(error) => Future.failed(error.httpError.internalReason.getOrElse(new RuntimeException(error.httpError.toString)))
      case Right(v)    => Future.successful(v)
    }

  def handleResultT(resultT: EitherT[Future, ServiceError, Result])(implicit ec: ExecutionContext, logger: Logger): Future[Result] =
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

  def getMaxMonthsWithinTaxYearOrRedirect(business: BusinessData, taxYear: TaxYear): Either[Result, Int] = {
    val taxYearCutoffDate = LocalDate.parse(s"${taxYear.endYear}-04-05")
    val defaultMaxMonths  = 12
    business.commencementDate.fold[Either[Result, Int]] {
      logger.error(s"Business businessId=${business.businessId} does not have a commencement date. Redirecting to journey recovery.")
      Left(redirectJourneyRecovery())
    } { date =>
      val startDate: LocalDate                 = LocalDate.parse(date)
      val monthsBetweenStartDateAndCutOff: Int = ChronoUnit.MONTHS.between(startDate, taxYearCutoffDate).toInt
      monthsBetweenStartDateAndCutOff match {
        case m if m < 0                => Left(redirectJourneyRecovery())
        case m if m > defaultMaxMonths => Right(defaultMaxMonths)
        case m                         => Right(m)
      }
    }
  }
}
