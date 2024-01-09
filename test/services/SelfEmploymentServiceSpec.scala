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
import cats.data.EitherT
import cats.implicits.catsSyntaxEitherId
import connectors.SelfEmploymentConnector
import controllers.actions.SubmittedDataRetrievalActionProvider
import models.common._
import models.database.UserAnswers
import models.errors.ServiceError.{ConnectorResponseError, NotFoundError}
import models.errors.{HttpError, HttpErrorBody, ServiceError}
import models.journeys.Journey.ExpensesGoodsToSellOrUse
import models.journeys.{Journey, JourneyNameAndStatus}
import org.mockito.ArgumentMatchersSugar
import org.mockito.IdiomaticMockito.StubbingOps
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import pages.income.TurnoverIncomeAmountPage
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import repositories.SessionRepository
import services.SelfEmploymentService.getIncomeTradingAllowance

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class SelfEmploymentServiceSpec extends SpecBase with MockitoSugar with ArgumentMatchersSugar {

  val mockConnector: SelfEmploymentConnector   = mock[SelfEmploymentConnector]
  val mockSessionRepository                    = mock[SessionRepository]
  val mockSubmittedDataRetrievalActionProvider = mock[SubmittedDataRetrievalActionProvider]

  val service: SelfEmploymentService = new SelfEmploymentService(mockConnector, mockSessionRepository)

  val nino              = Nino("nino")
  val businessIdAccrual = BusinessId("businessIdAccrual")
  val businessIdCash    = BusinessId("businessIdCash")

  val maxIncomeTradingAllowance: BigDecimal = 1000
  val smallTurnover: BigDecimal             = 450.00
  val largeTurnover: BigDecimal             = 45000.00

  "getJourneyStatus" - {
    "should return status" in {
      val status = JourneyNameAndStatus(ExpensesGoodsToSellOrUse, JourneyStatus.Completed)
      mockConnector.getJourneyState(any[BusinessId], any[Journey], any[TaxYear], any[Mtditid])(*, *) returns EitherT
        .rightT[Future, ServiceError](status)

      val result = service.getJourneyStatus(JourneyAnswersContext(taxYear, businessId, Mtditid(mtditid), ExpensesGoodsToSellOrUse)).value.futureValue

      result shouldBe status.journeyStatus.asRight
    }
  }

  "setJourneyStatus" - {
    "should save status" in {
      mockConnector.saveJourneyState(any[JourneyAnswersContext], any[JourneyStatus])(*, *) returns EitherT.rightT[Future, ServiceError](())
      val result = service
        .setJourneyStatus(JourneyAnswersContext(taxYear, businessId, Mtditid(mtditid), ExpensesGoodsToSellOrUse), JourneyStatus.Completed)
        .value
        .futureValue
      result shouldBe ().asRight
    }
  }

  "getBusinessAccountingType" - {
    "should return a BusinessID's accounting type in a Right when this is returned from the backend" in {
      mockConnector.getBusiness(nino.value, businessIdAccrual, mtditid) returns Future.successful(Right(aBusinessData))
      mockConnector.getBusiness(nino.value, businessIdCash, mtditid) returns Future.successful(Right(aBusinessDataCashAccounting))

      val resultAccrual = await(service.getAccountingType(nino.value, businessIdAccrual, mtditid))
      val resultCash    = await(service.getAccountingType(nino.value, businessIdCash, mtditid))

      resultAccrual shouldBe Right(AccountingType.Accrual)
      resultCash shouldBe Right(AccountingType.Cash)
    }

    "should return an error when" - {

      "an empty sequence is returned from the backend" in {
        mockConnector.getBusiness(nino.value, businessIdAccrual, mtditid) returns Future.successful(Right(Seq.empty))

        val result = await(service.getAccountingType(nino.value, businessIdAccrual, mtditid))

        result shouldBe Left(NotFoundError("Business not found"))
      }

      "an error is returned from the backend" in {
        mockConnector.getBusiness(nino.value, businessIdAccrual, mtditid) returns Future.successful(
          Left(ConnectorResponseError("method", "url", HttpError(INTERNAL_SERVER_ERROR, HttpErrorBody.parsingError))))

        val result = await(service.getAccountingType(nino.value, businessIdAccrual, mtditid))(10.seconds)

        result shouldBe Left(ConnectorResponseError("method", "url", HttpError(INTERNAL_SERVER_ERROR, HttpErrorBody.parsingError)))
      }
    }
  }

  "getIncomeTradingAllowance" - {
    "should return a BigDecimal trading allowance that is" - {
      "equal to the turnover amount when the turnover amount is less than the max trading allowance" in {
        val userAnswers = UserAnswers(userAnswersId).set(TurnoverIncomeAmountPage, smallTurnover, Some(businessId)).success.value

        getIncomeTradingAllowance(businessId, userAnswers) mustEqual smallTurnover
      }

      "equal to the max allowance when the turnover amount is equal or greater than the max trading allowance" in {
        val userAnswersLargeTurnover =
          UserAnswers(userAnswersId).set(TurnoverIncomeAmountPage, largeTurnover, Some(businessId)).success.value
        val userAnswersEqualToMax =
          UserAnswers(userAnswersId).set(TurnoverIncomeAmountPage, maxIncomeTradingAllowance, Some(businessId)).success.value

        getIncomeTradingAllowance(businessId, userAnswersLargeTurnover) shouldBe maxIncomeTradingAllowance
        getIncomeTradingAllowance(businessId, userAnswersEqualToMax) shouldBe maxIncomeTradingAllowance
      }
    }
  }

  "submitAnswers" - {
    val userAnswerData = Json
      .parse(s"""
           |{
           |  "$businessId": {
           |  }
           |}
           |""".stripMargin)
      .as[JsObject]
    val userAnswers: UserAnswers = UserAnswers(userAnswersId, userAnswerData)
    val ctx                      = JourneyAnswersContext(taxYear, businessId, Mtditid(mtditid), ExpensesGoodsToSellOrUse)
    mockConnector.submitAnswers(any, any)(*, *, *) returns EitherT(Future.successful(().asRight[ServiceError]))

    "submit answers to the connector" in {
      val result = service.submitAnswers[JsObject](ctx, userAnswers).value.futureValue
      result shouldBe ().asRight
    }
  }

}
