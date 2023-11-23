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
import models.errors.HttpError
import models.errors.HttpErrorBody.SingleErrorBody
import models.journeys.Journey.ExpensesGoodsToSellOrUse
import models.journeys.expenses.ExpensesData
import models.journeys.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status.{BAD_REQUEST, NO_CONTENT}
import play.api.libs.json.Json

class SelfEmploymentConnectorISpec extends WiremockSpec with IntegrationBaseSpec {

  private val someExpensesJourney = ExpensesGoodsToSellOrUse
  private val someExpensesData    = ExpensesData(taxYear, nino, businessId, someExpensesJourney, mtditid)

  private val downstreamUrl =
    s"/income-tax-self-employment/send-expenses-answers/${someExpensesData.journey.toString}" +
      s"/${someExpensesData.taxYear.value}/${someExpensesData.businessId.value}/${someExpensesData.nino.value}"

  private val someExpensesJourneyAnswers =
    GoodsToSellOrUseJourneyAnswers(goodsToSellOrUseAmount = 100.00, disallowableGoodsToSellOrUseAmount = Some(100.00))

  private val httpError = HttpError(BAD_REQUEST, SingleErrorBody("PARSING_ERROR", "Error parsing response from CONNECTOR"))

  private val connector = new SelfEmploymentConnector(httpClient, appConfig)

  "sending expenses answers" must {
    "return a successful result when downstream returns a success response" in {
      stubPostWithRequestBody(
        url = downstreamUrl,
        requestBody = Json.toJson(someExpensesJourneyAnswers),
        expectedStatus = NO_CONTENT,
        requestHeaders = headersSentToBE)

      await(
        connector
          .sendExpensesAnswers(someExpensesData, someExpensesJourneyAnswers)(hc, ec, GoodsToSellOrUseJourneyAnswers.writes)) shouldBe ().asRight
    }
    "return a failure result when downstream returns a error" in {
      stubPostWithRequestBody(
        url = downstreamUrl,
        requestBody = Json.toJson(someExpensesJourneyAnswers),
        expectedStatus = BAD_REQUEST,
        requestHeaders = headersSentToBE)

      await(
        connector
          .sendExpensesAnswers(someExpensesData, someExpensesJourneyAnswers)(hc, ec, GoodsToSellOrUseJourneyAnswers.writes)) shouldBe httpError.asLeft
    }
  }

}