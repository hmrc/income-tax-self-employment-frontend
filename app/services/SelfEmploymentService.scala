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

import cats.data.EitherT
import connectors.SelfEmploymentConnector
import models.common._
import models.database.UserAnswers
import models.domain.ApiResultT
import models.errors.{HttpError, HttpErrorBody}
import models.journeys.Journey
import models.journeys.Journey.{ExpensesTailoring, TradeDetails}
import models.requests.TradesJourneyStatuses
import pages.QuestionPage
import pages.income.TurnoverIncomeAmountPage
import play.api.Logging
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.{Format, Writes}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

// TODO Remove Base, and rename SelfEmploymentService to have Impl suffix
trait SelfEmploymentServiceBase {
  def getJourneyStatus(journey: Journey, nino: Nino, taxYear: TaxYear, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[JourneyStatus]
  def getCompletedTradeDetails(nino: Nino, taxYear: TaxYear, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[List[TradesJourneyStatuses]]
  def getAccountingType(nino: String, businessId: BusinessId, mtditid: String)(implicit hc: HeaderCarrier): Future[Either[HttpError, String]]
  def saveAnswer[A: Writes](businessId: BusinessId, userAnswers: UserAnswers, value: A, page: QuestionPage[A]): Future[UserAnswers]
  def submitAnswers[SubsetOfAnswers: Format](taxYear: TaxYear, businessId: BusinessId, mtditid: Mtditid, userAnswers: UserAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit]
}

class SelfEmploymentService @Inject() (connector: SelfEmploymentConnector, sessionRepository: SessionRepository)(implicit ec: ExecutionContext)
    extends SelfEmploymentServiceBase
    with Logging {

  def getJourneyStatus(journey: Journey, nino: Nino, taxYear: TaxYear, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[JourneyStatus] = {
    val tradeId   = BusinessId(s"${TradeDetails.toString}-${nino.value}")
    val journeyId = journey.toString

    EitherT(connector.getJourneyState(tradeId, journeyId, taxYear, mtditid.value))
      .map(JourneyStatus.tradeDetailsStatusFromCompletedState)
  }

  def getCompletedTradeDetails(nino: Nino, taxYear: TaxYear, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[List[TradesJourneyStatuses]] =
    EitherT(connector.getCompletedTradesWithStatuses(nino.value, taxYear, mtditid.value))

  // TODO return AccountingType
  // TODO HttpErrors in business layer may not be the best idea
  def getAccountingType(nino: String, businessId: BusinessId, mtditid: String)(implicit hc: HeaderCarrier): Future[Either[HttpError, String]] =
    connector.getBusiness(nino, businessId, mtditid).map {
      case Right(businesses) if businesses.exists(_.accountingType.nonEmpty) => Right(businesses.head.accountingType.get)
      case Left(error)                                                       => Left(error)
      case _ => Left(HttpError(NOT_FOUND, HttpErrorBody.SingleErrorBody("404", "Business not found")))
    }

  def saveAnswer[A: Writes](businessId: BusinessId, userAnswers: UserAnswers, value: A, page: QuestionPage[A]): Future[UserAnswers] =
    for {
      updatedAnswers <- Future.fromTry(userAnswers.set[A](page, value, Some(businessId)))
      _              <- sessionRepository.set(updatedAnswers)
    } yield updatedAnswers

  def submitAnswers[SubsetOfAnswers: Format](taxYear: TaxYear, businessId: BusinessId, mtditid: Mtditid, userAnswers: UserAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val journeyAnswers: SubsetOfAnswers = (userAnswers.data \ businessId.value).as[SubsetOfAnswers]
    connector.submitAnswers(taxYear, businessId, mtditid, ExpensesTailoring, journeyAnswers)
  }
}

object SelfEmploymentService {

  private val maxIncomeTradingAllowance: BigDecimal = 1000

  def getIncomeTradingAllowance(businessId: BusinessId, userAnswers: UserAnswers): BigDecimal = {
    val turnover: BigDecimal = userAnswers.get(TurnoverIncomeAmountPage, Some(businessId)).getOrElse(maxIncomeTradingAllowance)
    if (turnover > maxIncomeTradingAllowance) maxIncomeTradingAllowance else turnover
  }
}
