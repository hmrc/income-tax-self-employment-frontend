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

package services

import cats.implicits.catsSyntaxEitherId
import connectors.SelfEmploymentConnector
import controllers.redirectJourneyRecovery
import models.common._
import models.database.UserAnswers
import models.domain.ApiResultT
import models.errors.ServiceError
import models.errors.ServiceError.NotFoundError
import pages.QuestionPage
import pages.income.TurnoverIncomeAmountPage
import play.api.Logging
import play.api.libs.json.{Format, Writes}
import play.api.mvc.Result
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait SelfEmploymentService {
  def getJourneyStatus(ctx: JourneyAnswersContext)(implicit hc: HeaderCarrier): ApiResultT[JourneyStatus]
  def setJourneyStatus(ctx: JourneyAnswersContext, status: JourneyStatus)(implicit hc: HeaderCarrier): ApiResultT[Unit]
  def getAccountingType(nino: Nino, businessId: BusinessId, mtditid: Mtditid)(implicit
      hc: HeaderCarrier): Future[Either[ServiceError, AccountingType]]
  def persistAnswer[A: Writes](businessId: BusinessId, userAnswers: UserAnswers, value: A, page: QuestionPage[A]): Future[UserAnswers]
  def submitAnswers[SubsetOfAnswers: Format](context: JourneyContext, userAnswers: UserAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit]
}

class SelfEmploymentServiceImpl @Inject() (
    connector: SelfEmploymentConnector,
    sessionRepository: SessionRepository
)(implicit ec: ExecutionContext)
    extends SelfEmploymentService
    with Logging {

  def getJourneyStatus(ctx: JourneyAnswersContext)(implicit hc: HeaderCarrier): ApiResultT[JourneyStatus] =
    connector.getJourneyState(ctx.businessId, ctx.journey, ctx.taxYear, ctx.mtditid).map(_.journeyStatus)

  def setJourneyStatus(ctx: JourneyAnswersContext, status: JourneyStatus)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    connector.saveJourneyState(ctx, status)

  // TODO HttpErrors in business layer may not be the best idea
  def getAccountingType(nino: Nino, businessId: BusinessId, mtditid: Mtditid)(implicit
      hc: HeaderCarrier): Future[Either[ServiceError, AccountingType]] =
    connector.getBusiness(nino, businessId, mtditid).map {
      case Right(businesses) if businesses.exists(_.accountingType.nonEmpty) => Right(AccountingType.withName(businesses.head.accountingType.get))
      case Left(error)                                                       => Left(error)
      case _                                                                 => Left(NotFoundError("Business not found"))
    }

  def persistAnswer[SubsetOfAnswers: Writes](businessId: BusinessId,
                                             userAnswers: UserAnswers,
                                             value: SubsetOfAnswers,
                                             page: QuestionPage[SubsetOfAnswers]): Future[UserAnswers] =
    for {
      updatedAnswers <- Future.fromTry(userAnswers.set[SubsetOfAnswers](page, value, Some(businessId)))
      _              <- sessionRepository.set(updatedAnswers)
    } yield updatedAnswers

  def submitAnswers[SubsetOfAnswers: Format](context: JourneyContext, userAnswers: UserAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val journeyAnswers: SubsetOfAnswers = (userAnswers.data \ context.businessId.value).as[SubsetOfAnswers]
    connector.submitAnswers(context, journeyAnswers)
  }
}

object SelfEmploymentService {

  private val maxAllowance = BigDecimal(1000.00)

  // TODO: Return an error as left once we have impl. error handling instead of redirecting.
  def getMaxTradingAllowance(businessId: BusinessId, userAnswers: UserAnswers): Either[Result, BigDecimal] =
    userAnswers
      .get(TurnoverIncomeAmountPage, Some(businessId))
      .fold(redirectJourneyRecovery().asLeft[BigDecimal]) { turnover =>
        if (turnover > maxAllowance) maxAllowance.asRight else turnover.asRight
      }
}
