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
import cats.implicits.catsSyntaxEitherId
import helpers.WiremockSpec
import models.common.{Mtditid, SubmissionContext}
import models.journeys.Journey.ExpensesGoodsToSellOrUse
import helpers.{PagerDutyAware, WiremockSpec}
import models.errors.HttpError
import models.errors.HttpErrorBody.SingleErrorBody
import models.journeys.Journey.{ExpensesGoodsToSellOrUse, ExpensesTailoring}
import models.journeys.expenses.ExpensesData
import models.journeys.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status.{BAD_REQUEST, NO_CONTENT}
import play.api.libs.json.{JsObject, Json}
import utils.PagerDutyHelper.PagerDutyKeys.FOURXX_RESPONSE_FROM_CONNECTOR

class SelfEmploymentConnectorISpec extends WiremockSpec with IntegrationBaseSpec with BeforeAndAfterEach {

  private val someExpensesJourney = ExpensesGoodsToSellOrUse
  private val ctx                 = SubmissionContext(taxYear, nino, businessId, mtditid, someExpensesJourney)

  private val downstreamUrl =
    s"/income-tax-self-employment/send-journey-answers/${ctx.journey.toString}" +
      s"/${ctx.taxYear.value}/${ctx.businessId.value}/${ctx.nino.value}"

  private val someExpensesJourneyAnswers =
    GoodsToSellOrUseJourneyAnswers(goodsToSellOrUseAmount = 100.00, disallowableGoodsToSellOrUseAmount = Some(100.00))

  private val connector = new SelfEmploymentConnector(httpClient, appConfig)

  "sending journey answers" must {
    "return a successful result when downstream returns a success response" in {
      stubPostWithRequestBody(
        url = downstreamUrl,
        requestBody = Json.toJson(someExpensesJourneyAnswers),
        expectedStatus = NO_CONTENT,
        requestHeaders = headersSentToBE)

      await(
        connector
          .sendJourneyAnswers(ctx, someExpensesJourneyAnswers)(hc, ec, GoodsToSellOrUseJourneyAnswers.formats)) shouldBe ().asRight
    }

    // TODO check pager duty in SASS-6363
    "return a failure result when downstream returns a error" in {
      stubPostWithRequestBody(
        url = downstreamUrl,
        requestBody = Json.toJson(someExpensesJourneyAnswers),
        expectedStatus = BAD_REQUEST,
        requestHeaders = headersSentToBE)

      await(
        connector
          .sendJourneyAnswers(ctx, someExpensesJourneyAnswers)(hc, ec, GoodsToSellOrUseJourneyAnswers.formats)) shouldBe httpError.asLeft
    }
  }

  "submitAnswers" must {
    "return a successful result" in {
      stubPost(url = s"/income-tax-self-employment/$taxYear/$businessId/expenses-categories/answers", NO_CONTENT)
      val result = connector.submitAnswers[JsObject](taxYear, businessId, mtditid, ExpensesTailoring, JsObject.empty).value.futureValue
      result shouldBe ().asRight
    }

    "notify pager duty on failure" in new PagerDutyAware {
      stubPost(url = s"/income-tax-self-employment/$taxYear/$businessId/expenses-categories/answers", BAD_REQUEST)
      val result = connector.submitAnswers[JsObject](taxYear, businessId, mtditid, ExpensesTailoring, JsObject.empty).value.futureValue
      result shouldBe httpError.asLeft
      loggedErrors.exists(_.contains(FOURXX_RESPONSE_FROM_CONNECTOR.toString)) shouldBe true
    }
  }

}
