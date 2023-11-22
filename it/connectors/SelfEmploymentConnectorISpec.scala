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
import models.journeys.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status.{BAD_REQUEST, NO_CONTENT}
import play.api.libs.json.Json

class SelfEmploymentConnectorISpec extends WiremockSpec with IntegrationBaseSpec {

  private val downstreamUrl = s"/income-tax-self-employment/send-expenses-answers/${taxYear.value}/${businessId.value}/${nino.value}"

  private val goodsToSellJourneyAnswers =
    GoodsToSellOrUseJourneyAnswers(goodsToSellOrUseAmount = 100.00, disallowableGoodsToSellOrUseAmount = Some(100.00))

  private val httpError = HttpError(BAD_REQUEST, SingleErrorBody("PARSING_ERROR", "Error parsing response from CONNECTOR"))

  private val connector = new SelfEmploymentConnector(httpClient, appConfig)

  "SelfEmploymentConnector" when {
    "sending expenses answers" when {
      "downstream returns a success response" must {
        "return a successful result" in {
          stubPostWithRequestBody(
            url = downstreamUrl,
            requestBody = Json.toJson(goodsToSellJourneyAnswers),
            expectedStatus = NO_CONTENT,
            requestHeaders = headersSentToBE)

          await(
            connector.sendExpensesAnswers(taxYear, businessId, nino, mtditid, goodsToSellJourneyAnswers)(
              hc,
              ec,
              GoodsToSellOrUseJourneyAnswers.writes)) shouldBe ().asRight
        }
      }
      "downstream returns an error" must {
        "return a failure result" in {
          stubPostWithRequestBody(
            url = downstreamUrl,
            requestBody = Json.toJson(goodsToSellJourneyAnswers),
            expectedStatus = BAD_REQUEST,
            requestHeaders = headersSentToBE)

          await(
            connector.sendExpensesAnswers(taxYear, businessId, nino, mtditid, goodsToSellJourneyAnswers)(
              hc,
              ec,
              GoodsToSellOrUseJourneyAnswers.writes)) shouldBe httpError.asLeft
        }
      }
    }
  }

}
