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
import models.domain.{ApiResultT, BusinessData}
import models.errors.ServiceError.NotFoundError
import pages.income.TurnoverIncomeAmountPage
import pages.{QuestionPage, TradeAccountingType}
import play.api.Logging
import play.api.libs.json.{Format, Writes}
import play.api.mvc.Result
import queries.Settable
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.annotation.{nowarn, tailrec}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait SelfEmploymentService {
  def getBusinesses(nino: Nino, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[Seq[BusinessData]]
  def getBusiness(nino: Nino, businessId: BusinessId, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[BusinessData]
  def getJourneyStatus(ctx: JourneyAnswersContext)(implicit hc: HeaderCarrier): ApiResultT[JourneyStatus]
  def setJourneyStatus(ctx: JourneyAnswersContext, status: JourneyStatus)(implicit hc: HeaderCarrier): ApiResultT[Unit]
  def getAccountingType(nino: Nino, businessId: BusinessId, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[AccountingType]
  def persistAnswer[A: Writes](businessId: BusinessId, userAnswers: UserAnswers, value: A, page: QuestionPage[A]): Future[UserAnswers]
  def submitAnswers[SubsetOfAnswers: Format](context: JourneyContext, userAnswers: UserAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit]
  def setAccountingTypeForIds(userAnswers: UserAnswers, pairedIdsAndAccounting: Seq[(AccountingType, BusinessId)]): Future[UserAnswers]
}

class SelfEmploymentServiceImpl @Inject() (
    connector: SelfEmploymentConnector,
    sessionRepository: SessionRepository
)(implicit ec: ExecutionContext)
    extends SelfEmploymentService
    with Logging {

  def getBusinesses(nino: Nino, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[Seq[BusinessData]] =
    connector.getBusinesses(nino, mtditid)

  def getBusiness(nino: Nino, businessId: BusinessId, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[BusinessData] =
    connector.getBusiness(nino, businessId, mtditid).map(_.headOption).subflatMap {
      case Some(value) => value.asRight
      case None        => NotFoundError(s"Unable to find business with ID: $businessId").asLeft
    }

  def getJourneyStatus(ctx: JourneyAnswersContext)(implicit hc: HeaderCarrier): ApiResultT[JourneyStatus] =
    connector.getJourneyState(ctx.businessId, ctx.journey, ctx.taxYear, ctx.mtditid).map(_.journeyStatus)

  def setJourneyStatus(ctx: JourneyAnswersContext, status: JourneyStatus)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    connector.saveJourneyState(ctx, status)

  def getAccountingType(nino: Nino, businessId: BusinessId, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[AccountingType] =
    getBusiness(nino, businessId, mtditid).map(business => business.accountingType).subflatMap {
      case Some("ACCRUAL")     => AccountingType.Accrual.asRight
      case Some("CASH")        => AccountingType.Cash.asRight
      case Some(invalidString) => NotFoundError(s"Accounting type of business with ID: $businessId has invalid value: $invalidString").asLeft
      case None                => NotFoundError(s"Unable to find accounting type of business with ID: $businessId").asLeft
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

  @nowarn("msg=match may not be exhaustive")
  def setAccountingTypeForIds(userAnswers: UserAnswers, pairedIdsAndAccounting: Seq[(AccountingType, BusinessId)]): Future[UserAnswers] =
    pairedIdsAndAccounting match {
      case Nil =>
        Future(userAnswers)
      case (accountingType: AccountingType, businessId: BusinessId) :: tail =>
        persistAnswer(businessId, userAnswers, accountingType, TradeAccountingType) flatMap { updatedUserAnswers: UserAnswers =>
          setAccountingTypeForIds(updatedUserAnswers, tail)
        }
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

  def clearDataFromUserAnswers(userAnswers: UserAnswers, pages: List[Settable[_]], businessId: Option[BusinessId]): Try[UserAnswers] = {
    @tailrec
    def removePageData(userAnswers: UserAnswers, pages: List[Settable[_]]): Try[UserAnswers] =
      pages match {
        case Nil =>
          Try(userAnswers)
        case head :: tail =>
          userAnswers.remove(head, businessId) match {
            case Success(updatedUserAnswers) =>
              removePageData(updatedUserAnswers, tail)
            case Failure(exception) =>
              Failure(exception)
          }
      }

    removePageData(userAnswers, pages)
  }

}
