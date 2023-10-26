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

import base.SpecBase
import builders.BusinessDataBuilder.{aBusinessData, aBusinessDataCashAccounting}
import builders.TradesJourneyStatusesBuilder.aSequenceTadesJourneyStatusesModel
import connectors.SelfEmploymentConnector
import models.UserAnswers
import models.errors.{HttpError, HttpErrorBody}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.income.TurnoverIncomeAmountPage
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.test.Helpers.await
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class SelfEmploymentServiceSpec extends SpecBase with MockitoSugar {

  val mockConnector: SelfEmploymentConnector = mock[SelfEmploymentConnector]
  val service: SelfEmploymentService         = new SelfEmploymentService(mockConnector)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val nino              = "nino"
  val businessIdAccrual = "businessIdAccrual"
  val businessIdCash    = "businessIdCash"
  val mtditid           = "mtditid"
  val accrual           = "ACCRUAL"
  val cash              = "CASH"

  val maxIncomeTradingAllowance: BigDecimal = 1000
  val smallTurnover: BigDecimal             = 450.00
  val largeTurnover: BigDecimal             = 45000.00
  val businessId                            = "businessId"

  "getCompletedTradeDetails" - {
    "should return a Right(Seq(TradesJourneyStatuses)) when this is returned from the backend" in {
      when(mockConnector.getCompletedTradesWithStatuses(meq(nino), meq(taxYear), meq(mtditid))(any, any)) thenReturn Future(
        Right(aSequenceTadesJourneyStatusesModel))

      val result = await(service.getCompletedTradeDetails(nino, taxYear, mtditid))(10.seconds)

      result mustEqual Right(aSequenceTadesJourneyStatusesModel)
    }
    "should return a Left(HttpError) when a this is returned from the backend" in {
      when(mockConnector.getCompletedTradesWithStatuses(meq(nino), meq(taxYear), meq(mtditid))(any, any)) thenReturn Future(
        Left(HttpError(404, HttpErrorBody.parsingError)))

      val result = await(service.getCompletedTradeDetails(nino, taxYear, mtditid))(10.seconds)

      result mustEqual Left(HttpError(404, HttpErrorBody.parsingError))
    }
  }

  "getBusinessAccountingType" - {
    "should return a BusinessID's accounting type in a Right when this is returned from the backend" in {
      when(mockConnector.getBusiness(meq(nino), meq(businessIdAccrual), meq(mtditid))(any, any)) thenReturn Future(Right(aBusinessData))
      when(mockConnector.getBusiness(meq(nino), meq(businessIdCash), meq(mtditid))(any, any)) thenReturn Future(Right(aBusinessDataCashAccounting))

      val resultAccrual = await(service.getAccountingType(nino, businessIdAccrual, mtditid))(10.seconds)
      val resultCash    = await(service.getAccountingType(nino, businessIdCash, mtditid))(10.seconds)

      resultAccrual mustEqual Right(accrual)
      resultCash mustEqual Right(cash)
    }

    "should return a Left(HttpError) when" - {

      "an empty sequence is returned from the backend" in {
        when(mockConnector.getBusiness(meq(nino), meq(businessIdAccrual), meq(mtditid))(any, any)) thenReturn Future(Right(Seq.empty))

        val result = await(service.getAccountingType(nino, businessIdAccrual, mtditid))(10.seconds)

        result mustEqual Left(HttpError(NOT_FOUND, HttpErrorBody.SingleErrorBody("404", "Business not found")))
      }

      "a Left(HttpError) is returned from the backend" in {
        when(mockConnector.getBusiness(meq(nino), meq(businessIdAccrual), meq(mtditid))(any, any)) thenReturn Future(
          Left(HttpError(INTERNAL_SERVER_ERROR, HttpErrorBody.parsingError)))

        val result = await(service.getAccountingType(nino, businessIdAccrual, mtditid))(10.seconds)

        result mustEqual Left(HttpError(INTERNAL_SERVER_ERROR, HttpErrorBody.parsingError))
      }
    }
  }

  "getIncomeTradingAllowance" - {
    "should return a BigDecimal trading allowance that is" - {
      "equal to the turnover amount when the turnover amount is less than the max trading allowance" in {
        val userAnswers = UserAnswers(userAnswersId).set(TurnoverIncomeAmountPage, smallTurnover, Some(businessId)).success.value

        service.getIncomeTradingAllowance(businessId, userAnswers) mustEqual smallTurnover
      }

      "equal to the max allowance when the turnover amount is equal or greater than the max trading allowance" in {
        val userAnswersLargeTurnover =
          UserAnswers(userAnswersId).set(TurnoverIncomeAmountPage, largeTurnover, Some(businessId)).success.value
        val userAnswersEqualToMax =
          UserAnswers(userAnswersId).set(TurnoverIncomeAmountPage, maxIncomeTradingAllowance, Some(businessId)).success.value

        service.getIncomeTradingAllowance(businessId, userAnswersLargeTurnover) mustEqual maxIncomeTradingAllowance
        service.getIncomeTradingAllowance(businessId, userAnswersEqualToMax) mustEqual maxIncomeTradingAllowance
      }
    }
  }

  "convertBigDecimalToMoneyString" - {
    "should format BigDecimals to String with commas every thousand, and to two decimal places unless a whole number" in {
      val bigDecimalSeq: Seq[BigDecimal]  = Seq(1000000000, 1000.00, 1000.1, 1000.10, 1000.01, 0.1)
      val formattedStringSeq: Seq[String] = Seq("1,000,000,000", "1,000", "1,000.10", "1,000.10", "1,000.01", "0.10")

      bigDecimalSeq.map(service.convertBigDecimalToMoneyString(_)) mustEqual formattedStringSeq
    }
  }

}
