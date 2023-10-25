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

import connectors.SelfEmploymentConnector
import connectors.httpParser.GetTradesStatusHttpParser.GetTradesStatusResponse
import models.UserAnswers
import models.errors.{HttpError, HttpErrorBody}
import pages.income.TurnoverIncomeAmountPage
import play.api.Logging
import play.api.http.Status.NOT_FOUND
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelfEmploymentService @Inject() (connector: SelfEmploymentConnector)(implicit ec: ExecutionContext) extends Logging {

  private val maxIncomeTradingAllowance: BigDecimal = 1000

  def getCompletedTradeDetails(nino: String, taxYear: Int, mtditid: String)(implicit hc: HeaderCarrier): Future[GetTradesStatusResponse] = {

    connector.getCompletedTradesWithStatuses(nino, taxYear, mtditid)
  }

  def getAccountingType(nino: String, businessId: String, mtditid: String)(implicit hc: HeaderCarrier): Future[Either[HttpError, String]] = {

    connector.getBusiness(nino, businessId, mtditid).map {
      case Right(businesses) if businesses.exists(_.accountingType.nonEmpty) => Right(businesses.head.accountingType.get)
      case Left(error)                                                       => Left(error)
      case _ => Left(HttpError(NOT_FOUND, HttpErrorBody.SingleErrorBody("404", "Business not found")))
    }
  }

  def getIncomeTradingAllowance(businessId: String, userAnswers: UserAnswers): BigDecimal = {
    val turnover: BigDecimal = userAnswers.get(TurnoverIncomeAmountPage, Some(businessId)).getOrElse(maxIncomeTradingAllowance)
    if (turnover > maxIncomeTradingAllowance) maxIncomeTradingAllowance else turnover
  }

}
