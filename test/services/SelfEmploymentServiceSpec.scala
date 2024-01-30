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
import models.domain.BusinessData
import models.errors.ServiceError.{ConnectorResponseError, NotFoundError}
import models.errors.{HttpError, HttpErrorBody, ServiceError}
import models.journeys.Journey.ExpensesGoodsToSellOrUse
import models.journeys.{Journey, JourneyNameAndStatus}
import org.mockito.ArgumentMatchersSugar
import org.mockito.IdiomaticMockito.StubbingOps
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.workplaceRunningCosts.workingFromBusinessPremises._
import pages.income.TurnoverIncomeAmountPage
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import repositories.SessionRepository
import services.SelfEmploymentService.{clearDataFromUserAnswers, getMaxTradingAllowance}

import scala.concurrent.Future

class SelfEmploymentServiceSpec extends SpecBase with MockitoSugar with ArgumentMatchersSugar {

  val mockConnector: SelfEmploymentConnector   = mock[SelfEmploymentConnector]
  val mockSessionRepository                    = mock[SessionRepository]
  val mockSubmittedDataRetrievalActionProvider = mock[SubmittedDataRetrievalActionProvider]

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

  "getAccountingType" - {
    "should return a BusinessID's accounting type in a Right when this is returned from the backend" in {
      mockConnector.getBusiness(nino, businessIdAccrual, mtditid) returns EitherT.rightT[Future, ServiceError](Seq(aBusinessData))
      mockConnector.getBusiness(nino, businessIdCash, mtditid) returns EitherT.rightT[Future, ServiceError](Seq(aBusinessDataCashAccounting))

      val resultAccrual = await(service.getAccountingType(nino, businessIdAccrual, mtditid).value)
      val resultCash    = await(service.getAccountingType(nino, businessIdCash, mtditid).value)

      resultAccrual shouldBe Right(AccountingType.Accrual)
      resultCash shouldBe Right(AccountingType.Cash)
    }

    "should return an error when" - {

      "an empty sequence is returned from the backend" in {
        mockConnector.getBusiness(nino, businessIdAccrual, mtditid) returns EitherT.rightT[Future, ServiceError](Seq.empty)

        val result = await(service.getAccountingType(nino, businessIdAccrual, mtditid).value)

        result shouldBe Left(NotFoundError(s"Unable to find business with ID: $businessIdAccrual"))
      }

      "an error is returned from the backend" in {
        mockConnector.getBusiness(nino, businessIdAccrual, mtditid) returns EitherT.leftT[Future, Seq[BusinessData]](
          ConnectorResponseError("method", "url", HttpError(INTERNAL_SERVER_ERROR, HttpErrorBody.parsingError)))

        val result = await(service.getAccountingType(nino, businessIdAccrual, mtditid).value)

        result shouldBe Left(ConnectorResponseError("method", "url", HttpError(INTERNAL_SERVER_ERROR, HttpErrorBody.parsingError)))
      }
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
        "livingAtBusinessPremises-onePerson"       -> 2,
        "livingAtBusinessPremises-twoPeople"       -> 1,
        "livingAtBusinessPremises-threePlusPeople" -> 5)
    }
    "should handle trying to clear data when there is none saved" in {
      val result = clearDataFromUserAnswers(emptyUserAnswers, fullPageList, None)

      result.success.value.data shouldBe Json.obj()
    }
  }

}
