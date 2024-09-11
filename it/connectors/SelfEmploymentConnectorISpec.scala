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

package connectors

import base.IntegrationBaseSpec
import cats.implicits._
import helpers.{PagerDutyAware, WiremockSpec}
import models.common.Journey.{ExpensesGoodsToSellOrUse, ExpensesTailoring, Income}
import models.common.{Journey, JourneyAnswersContext, JourneyContextWithNino, JourneyStatus}
import models.domain.BusinessIncomeSourcesSummary
import models.journeys.adjustments.NetBusinessProfitOrLossValues
import models.journeys.{JourneyNameAndStatus, TaskList}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import utils.PagerDutyHelper.PagerDutyKeys.FOURXX_RESPONSE_FROM_CONNECTOR

import java.time.LocalDate

class SelfEmploymentConnectorISpec extends WiremockSpec with IntegrationBaseSpec {

  private def journeyNinoCtx(journey: Journey) = JourneyContextWithNino(taxYear, nino, businessId, mtditid, journey)

  private def journeyCtx(journey: Journey) = JourneyAnswersContext(taxYear, nino, businessId, mtditid, journey)

  private def downstreamNinoUrl(journey: Journey) = s"/income-tax-self-employment/$taxYear/$businessId/$journey/$nino/answers"

  private def downstreamUrl(journey: Journey) = s"/income-tax-self-employment/$taxYear/$businessId/$journey/answers"

  private def statusUrl(journey: Journey) = s"/income-tax-self-employment/completed-section/$businessId/$journey/$taxYear"

  private val taskListUrl                      = s"/income-tax-self-employment/$taxYear/$nino/task-list"
  private val dateOfBirthUrl                   = s"/income-tax-self-employment/user-date-of-birth/$nino"
  private val businessSummariesUrl             = s"/income-tax-self-employment/$taxYear/business-income-sources-summaries/$nino"
  private val businessSummaryUrl               = s"/income-tax-self-employment/$taxYear/business-income-sources-summary/$nino/$businessId"
  private val netBusinessProfitOrLossValuesUrl = s"/income-tax-self-employment/$taxYear/net-business-profit-or-loss-values/$nino/$businessId"
  private val clearExpensesUrl                 = s"/income-tax-self-employment/$taxYear/clear-simplified-expenses-answers/$nino/$businessId"

  val aBusinessIncomeSourcesSummary = BusinessIncomeSourcesSummary(
    businessId.value,
    100,
    100,
    100,
    100,
    Some(100),
    Some(100),
    Some(100),
    100,
    100
  )

  lazy val aNetBusinessProfitOrLossValues: NetBusinessProfitOrLossValues = NetBusinessProfitOrLossValues(
    turnover = 100,
    incomeNotCountedAsTurnover = 20,
    totalExpenses = 50,
    netProfit = 400,
    netLoss = 0,
    balancingCharge = 10,
    goodsAndServicesForOwnUse = 50,
    disallowableExpenses = 10,
    totalAdditions = 60,
    capitalAllowances = 0,
    turnoverNotTaxableAsBusinessProfit = 50,
    totalDeductions = 70
  )

  private val connector = new SelfEmploymentConnector(httpClient, appConfig)

  "submitAnswers" must {
    "return a successful result for JourneyAnswersContext" in {
      stubPost(url = downstreamUrl(ExpensesTailoring), NO_CONTENT)
      val result = connector.submitAnswers[JsObject](journeyCtx(ExpensesTailoring), JsObject.empty).value.futureValue
      result shouldBe ().asRight
    }

    "return a successful result for JourneyAnswersWithNino" in {
      stubPost(url = downstreamNinoUrl(ExpensesGoodsToSellOrUse), NO_CONTENT)
      val result = connector.submitAnswers[JsObject](journeyNinoCtx(ExpensesGoodsToSellOrUse), JsObject.empty).value.futureValue
      result shouldBe ().asRight
    }

    "notify pager duty on failure for JourneyAnswersContext" in new PagerDutyAware {
      stubPost(url = downstreamUrl(ExpensesTailoring), BAD_REQUEST)
      val result = connector.submitAnswers[JsObject](journeyCtx(ExpensesTailoring), JsObject.empty).value.futureValue
      result shouldBe parsingError("POST", "http://localhost:11111/income-tax-self-employment/2024/someBusinessId/expenses-categories/answers").asLeft
      loggedErrors.exists(_.contains(FOURXX_RESPONSE_FROM_CONNECTOR.toString)) shouldBe true
    }

    "notify pager duty on failure for JourneyAnswersWithNino" in new PagerDutyAware {
      stubPost(url = downstreamNinoUrl(ExpensesGoodsToSellOrUse), BAD_REQUEST)
      val result = connector.submitAnswers[JsObject](journeyNinoCtx(ExpensesGoodsToSellOrUse), JsObject.empty).value.futureValue
      result shouldBe parsingError(
        "POST",
        "http://localhost:11111/income-tax-self-employment/2024/someBusinessId/expenses-goods-to-sell-or-use/someNino/answers").asLeft
      loggedErrors.exists(_.contains(FOURXX_RESPONSE_FROM_CONNECTOR.toString)) shouldBe true
    }
  }

  "getJourneyState" must {
    "return state" in {
      val incomeWithStatus = JourneyNameAndStatus(Income, JourneyStatus.CheckOurRecords)
      val body             = Json.stringify(Json.toJson(incomeWithStatus))
      stubGetWithResponseBody(statusUrl(Income), OK, body, headersSentToBE)

      val result = connector.getJourneyState(businessId, Income, taxYear, mtditid).value.futureValue

      result shouldBe incomeWithStatus.asRight
    }
  }

  "getTaskList" must {
    "return a task list" in {
      val taskList = TaskList.empty
      val body     = Json.stringify(Json.toJson(taskList))
      stubGetWithResponseBody(taskListUrl, OK, body, headersSentToBE)

      val result = connector.getTaskList(nino, taxYear, mtditid).value.futureValue

      result shouldBe taskList.asRight
    }
  }

  "saveJourneyState" must {
    "save the new status" in {
      stubPutWithResponseBody(statusUrl(ExpensesTailoring), NO_CONTENT, "{}", headersSentToBE)
      val result = connector.saveJourneyState(journeyCtx(ExpensesTailoring), JourneyStatus.Completed).value.futureValue
      result shouldBe ().asRight
    }
  }

  "getSubmittedAnswers" must {
    "return answers" in {
      stubGetWithResponseBody(downstreamUrl(ExpensesTailoring), OK, "{}", headersSentToBE)
      val result = connector.getSubmittedAnswers[JsObject](journeyCtx(ExpensesTailoring)).value.futureValue
      result shouldBe JsObject.empty.some.asRight
    }

    "fail when the downstream service returns an error" in new PagerDutyAware {
      stubGetWithResponseBody(downstreamUrl(ExpensesTailoring), BAD_REQUEST, "{}", headersSentToBE)
      val result = connector.getSubmittedAnswers[JsObject](journeyCtx(ExpensesTailoring)).value.futureValue
      result shouldBe parsingError("GET", "http://localhost:11111/income-tax-self-employment/2024/someBusinessId/expenses-categories/answers").asLeft
      loggedErrors.exists(_.contains(FOURXX_RESPONSE_FROM_CONNECTOR.toString)) shouldBe true
    }
  }

  "getUserDateOfBirth" must {
    "return a user's date of birth" in {
      val userDateOfBirth = LocalDate.of(1997, 7, 30)
      val body            = Json.stringify(Json.toJson(userDateOfBirth))
      stubGetWithResponseBody(dateOfBirthUrl, OK, body, headersSentToBE)

      val result = connector.getUserDateOfBirth(nino, mtditid).value.futureValue

      result shouldBe userDateOfBirth.asRight
    }
  }

  "getAllBusinessIncomeSourcesSummaries" must {
    "return the summaries of any user businesses" in {
      val summariesList = List.empty[BusinessIncomeSourcesSummary]
      val body          = Json.stringify(Json.toJson(summariesList))
      stubGetWithResponseBody(businessSummariesUrl, OK, body, headersSentToBE)

      val result = connector.getAllBusinessIncomeSourcesSummaries(taxYear, nino, mtditid).value.futureValue

      result shouldBe summariesList.asRight
    }
  }

  "getBusinessIncomeSourcesSummary" must {
    "return the summary of a user business" in {
      val summary = aBusinessIncomeSourcesSummary
      val body    = Json.stringify(Json.toJson(summary))
      stubGetWithResponseBody(businessSummaryUrl, OK, body, headersSentToBE)

      val result = connector.getBusinessIncomeSourcesSummary(taxYear, nino, businessId, mtditid).value.futureValue

      result shouldBe summary.asRight
    }
  }

  "getNetBusinessProfitOrLossValues" must {
    "return net business profit values of a user business" in {
      val profitValues = aNetBusinessProfitOrLossValues
      val body         = Json.stringify(Json.toJson(profitValues))
      stubGetWithResponseBody(netBusinessProfitOrLossValuesUrl, OK, body, headersSentToBE)

      val result = connector.getNetBusinessProfitOrLossValues(taxYear, nino, businessId, mtditid).value.futureValue

      result shouldBe profitValues.asRight
    }
  }

  "clearExpensesSimplifiedOrNoExpensesAnswers" must {
    "return a successful result from downstream" in {
      stubPostWithoutResponseAndRequestBody(clearExpensesUrl, OK)

      val result = connector.clearExpensesSimplifiedOrNoExpensesAnswers(taxYear, nino, businessId, mtditid).value.futureValue

      result shouldBe ().asRight
    }
  }
}
