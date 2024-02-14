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
import cats.data.EitherT
import cats.implicits.catsSyntaxEitherId
import connectors.SelfEmploymentConnector
import controllers.actions.SubmittedDataRetrievalActionProvider
import models.common._
import models.database.UserAnswers
import models.errors.ServiceError
import models.journeys.Journey.ExpensesGoodsToSellOrUse
import models.journeys.{Journey, JourneyNameAndStatus}
import org.mockito.ArgumentMatchersSugar
import org.mockito.IdiomaticMockito.StubbingOps
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.workplaceRunningCosts.workingFromBusinessPremises._
import pages.income.TurnoverIncomeAmountPage
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import repositories.SessionRepository
import services.SelfEmploymentService.{clearDataFromUserAnswers, getMaxTradingAllowance}

import scala.concurrent.Future

class SelfEmploymentServiceSpec extends SpecBase with MockitoSugar with ArgumentMatchersSugar {

  val mockConnector: SelfEmploymentConnector   = mock[SelfEmploymentConnector]
  val mockSessionRepository                    = mock[SessionRepository]
  val mockSubmittedDataRetrievalActionProvider = mock[SubmittedDataRetrievalActionProvider]
  when(mockSessionRepository.set(any)) thenReturn Future.successful(true)

  val service: SelfEmploymentService = new SelfEmploymentServiceImpl(mockConnector, mockSessionRepository)

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

      val result = service.getJourneyStatus(JourneyAnswersContext(taxYear, businessId, mtditid, ExpensesGoodsToSellOrUse)).value.futureValue

      result shouldBe status.journeyStatus.asRight
    }
  }

  "setJourneyStatus" - {
    "should save status" in {
      mockConnector.saveJourneyState(any[JourneyAnswersContext], any[JourneyStatus])(*, *) returns EitherT.rightT[Future, ServiceError](())
      val result = service
        .setJourneyStatus(JourneyAnswersContext(taxYear, businessId, mtditid, ExpensesGoodsToSellOrUse), JourneyStatus.Completed)
        .value
        .futureValue
      result shouldBe ().asRight
    }
  }

  "getIncomeTradingAllowance" - {
    "should return a BigDecimal trading allowance that is" - {
      "equal to the turnover amount when the turnover amount is less than the max trading allowance" in {
        val userAnswers = UserAnswers(userAnswersId).set(TurnoverIncomeAmountPage, smallTurnover, Some(businessId)).success.value

        getMaxTradingAllowance(businessId, userAnswers) shouldBe smallTurnover.asRight
      }

      "equal to the max allowance when the turnover amount is equal or greater than the max trading allowance" in {
        val userAnswersLargeTurnover =
          UserAnswers(userAnswersId).set(TurnoverIncomeAmountPage, largeTurnover, Some(businessId)).success.value
        val userAnswersEqualToMax =
          UserAnswers(userAnswersId).set(TurnoverIncomeAmountPage, maxIncomeTradingAllowance, Some(businessId)).success.value

        getMaxTradingAllowance(businessId, userAnswersLargeTurnover) shouldBe maxIncomeTradingAllowance.asRight
        getMaxTradingAllowance(businessId, userAnswersEqualToMax) shouldBe maxIncomeTradingAllowance.asRight
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
    val ctx                      = JourneyAnswersContext(taxYear, businessId, mtditid, ExpensesGoodsToSellOrUse)
    mockConnector.submitAnswers(any, any)(*, *, *) returns EitherT(Future.successful(().asRight[ServiceError]))

    "submit answers to the connector" in {
      val result = service.submitAnswers[JsObject](ctx, userAnswers).value.futureValue
      result shouldBe ().asRight
    }
  }

  "clearDataFromUserAnswers" - {
    val userAnswers = emptyUserAnswers
      .set(LivingAtBusinessPremisesOnePerson, 2, None)
      .success
      .value
      .set(LivingAtBusinessPremisesTwoPeople, 1, None)
      .success
      .value
      .set(LivingAtBusinessPremisesThreePlusPeople, 5, None)
      .success
      .value
    val fullPageList = List(LivingAtBusinessPremisesOnePerson, LivingAtBusinessPremisesTwoPeople, LivingAtBusinessPremisesThreePlusPeople)
    val emptyList    = List.empty
    "should clear UserAnswers data from pages in the list and return the result" in {
      val result = clearDataFromUserAnswers(userAnswers, fullPageList, None)

      result.success.value.data shouldBe Json.obj()
    }
    "should handle an empty list of pages" in {
      val result = clearDataFromUserAnswers(userAnswers, emptyList, None)

      result.success.value.data shouldBe Json.obj(
        "livingAtBusinessPremisesOnePerson"       -> 2,
        "livingAtBusinessPremisesTwoPeople"       -> 1,
        "livingAtBusinessPremisesThreePlusPeople" -> 5)
    }
    "should handle trying to clear data when there is none saved" in {
      val result = clearDataFromUserAnswers(emptyUserAnswers, fullPageList, None)

      result.success.value.data shouldBe Json.obj()
    }
  }

  "setAccountingTypeForIds" - {
    "should set the AccountingType of each supplied BusinessId to the UserAnswers, returning the updated UserAnswers when" - {
      "supplied a valid sequence of BusinessIds and AccountingTypes" in {
        val testList = Seq(
          (AccountingType.Accrual, BusinessId("testId1")),
          (AccountingType.Cash, BusinessId("testId2")),
          (AccountingType.Accrual, BusinessId("testId3")))
        val result = await(service.setAccountingTypeForIds(emptyUserAnswers, testList)).data
        val expectedResult = Json.obj(
          "testId1" -> Json.obj("accountingType" -> "ACCRUAL"),
          "testId2" -> Json.obj("accountingType" -> "CASH"),
          "testId3" -> Json.obj("accountingType" -> "ACCRUAL"))

        result shouldBe expectedResult
      }
      "input sequence is empty" in {
        val result = await(service.setAccountingTypeForIds(emptyUserAnswers, Seq.empty)).data

        result shouldBe Json.obj()
      }
    }
  }

}
