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
import models.common.{JourneyAnswersContext, JourneyContextWithNino, JourneyStatus}
import models.journeys.{Journey, JourneyNameAndStatus, TaskList}
import models.journeys.Journey.{ExpensesGoodsToSellOrUse, ExpensesTailoring, Income}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import utils.PagerDutyHelper.PagerDutyKeys.FOURXX_RESPONSE_FROM_CONNECTOR

class SelfEmploymentConnectorISpec extends WiremockSpec with IntegrationBaseSpec {

  private def journeyNinoCtx(journey: Journey) = JourneyContextWithNino(taxYear, nino, businessId, mtditid, journey)
  private def journeyCtx(journey: Journey)     = JourneyAnswersContext(taxYear, businessId, mtditid, journey)

  private def downstreamNinoUrl(journey: Journey) = s"/income-tax-self-employment/$taxYear/$businessId/$journey/$nino/answers"
  private def downstreamUrl(journey: Journey)     = s"/income-tax-self-employment/$taxYear/$businessId/$journey/answers"
  private def statusUrl(journey: Journey)         = s"/income-tax-self-employment/completed-section/$businessId/$journey/$taxYear"
  private val taskListUrl                         = s"/income-tax-self-employment/$taxYear/$nino/task-list"

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
      result shouldBe parsingError.asLeft
      loggedErrors.exists(_.contains(FOURXX_RESPONSE_FROM_CONNECTOR.toString)) shouldBe true
    }

    "notify pager duty on failure for JourneyAnswersWithNino" in new PagerDutyAware {
      stubPost(url = downstreamNinoUrl(ExpensesGoodsToSellOrUse), BAD_REQUEST)
      val result = connector.submitAnswers[JsObject](journeyNinoCtx(ExpensesGoodsToSellOrUse), JsObject.empty).value.futureValue
      result shouldBe parsingError.asLeft
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

      val result = connector.getTaskList(nino.value, taxYear, mtditid).value.futureValue

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
      result shouldBe parsingError.asLeft
      loggedErrors.exists(_.contains(FOURXX_RESPONSE_FROM_CONNECTOR.toString)) shouldBe true
    }
  }

}
