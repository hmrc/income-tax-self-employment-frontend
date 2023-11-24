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
import models.database.UserAnswers
import models.errors.{HttpError, HttpErrorBody}
import org.mockito.ArgumentMatchersSugar
import org.mockito.IdiomaticMockito.StubbingOps
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import pages.income.TurnoverIncomeAmountPage
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import repositories.SessionRepository
import services.SelfEmploymentService.getIncomeTradingAllowance

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class SelfEmploymentServiceSpec extends SpecBase with MockitoSugar with ArgumentMatchersSugar {

  val mockConnector: SelfEmploymentConnector = mock[SelfEmploymentConnector]
  val mockSessionRepository                  = mock[SessionRepository]
  val service: SelfEmploymentService         = new SelfEmploymentService(mockConnector, mockSessionRepository)

  val nino              = "nino"
  val businessIdAccrual = "businessIdAccrual"
  val businessIdCash    = "businessIdCash"

  val maxIncomeTradingAllowance: BigDecimal = 1000
  val smallTurnover: BigDecimal             = 450.00
  val largeTurnover: BigDecimal             = 45000.00

  "getCompletedTradeDetails" - {
    "should return a Right(Seq(TradesJourneyStatuses)) when this is returned from the backend" in {
      mockConnector.getCompletedTradesWithStatuses(nino, taxYear, mtditid)(*, *) returns Future.successful(Right(aSequenceTadesJourneyStatusesModel))

      val result = await(service.getCompletedTradeDetails(nino, taxYear, mtditid))

      result shouldBe Right(aSequenceTadesJourneyStatusesModel)
    }
    "should return a Left(HttpError) when a this is returned from the backend" in {
      mockConnector.getCompletedTradesWithStatuses(nino, taxYear, mtditid)(*, *) returns Future.successful(
        Left(HttpError(404, HttpErrorBody.parsingError)))

      val result = await(service.getCompletedTradeDetails(nino, taxYear, mtditid))

      result shouldBe Left(HttpError(404, HttpErrorBody.parsingError))
    }
  }

  "getBusinessAccountingType" - {
    "should return a BusinessID's accounting type in a Right when this is returned from the backend" in {
      mockConnector.getBusiness(nino, businessIdAccrual, mtditid) returns Future.successful(Right(aBusinessData))
      mockConnector.getBusiness(nino, businessIdCash, mtditid) returns Future.successful(Right(aBusinessDataCashAccounting))

      val resultAccrual = await(service.getAccountingType(nino, businessIdAccrual, mtditid))
      val resultCash    = await(service.getAccountingType(nino, businessIdCash, mtditid))

      resultAccrual shouldBe Right(accrual)
      resultCash shouldBe Right(cash)
    }

    "should return a Left(HttpError) when" - {

      "an empty sequence is returned from the backend" in {
        mockConnector.getBusiness(nino, businessIdAccrual, mtditid) returns Future.successful(Right(Seq.empty))

        val result = await(service.getAccountingType(nino, businessIdAccrual, mtditid))

        result shouldBe Left(HttpError(NOT_FOUND, HttpErrorBody.SingleErrorBody("404", "Business not found")))
      }

      "a Left(HttpError) is returned from the backend" in {
        mockConnector.getBusiness(nino, businessIdAccrual, mtditid) returns Future.successful(
          Left(HttpError(INTERNAL_SERVER_ERROR, HttpErrorBody.parsingError)))

        val result = await(service.getAccountingType(nino, businessIdAccrual, mtditid))(10.seconds)

        result shouldBe Left(HttpError(INTERNAL_SERVER_ERROR, HttpErrorBody.parsingError))
      }
    }
  }

  "getIncomeTradingAllowance" - {
    "should return a BigDecimal trading allowance that is" - {
      "equal to the turnover amount when the turnover amount is less than the max trading allowance" in {
        val userAnswers = UserAnswers(userAnswersId).set(TurnoverIncomeAmountPage, smallTurnover, Some(stubbedBusinessId)).success.value

        getIncomeTradingAllowance(stubbedBusinessId, userAnswers) mustEqual smallTurnover
      }

      "equal to the max allowance when the turnover amount is equal or greater than the max trading allowance" in {
        val userAnswersLargeTurnover =
          UserAnswers(userAnswersId).set(TurnoverIncomeAmountPage, largeTurnover, Some(stubbedBusinessId)).success.value
        val userAnswersEqualToMax =
          UserAnswers(userAnswersId).set(TurnoverIncomeAmountPage, maxIncomeTradingAllowance, Some(stubbedBusinessId)).success.value

        getIncomeTradingAllowance(stubbedBusinessId, userAnswersLargeTurnover) shouldBe maxIncomeTradingAllowance
        getIncomeTradingAllowance(stubbedBusinessId, userAnswersEqualToMax) shouldBe maxIncomeTradingAllowance
      }
    }
  }

}
