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
import helpers.{PagerDutyAware, WiremockSpec}
import models.common.{JourneyAnswersContext, JourneyAnswersWithNino}
import models.journeys.Journey
import models.journeys.Journey.{ExpensesGoodsToSellOrUse, ExpensesTailoring}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status.{BAD_REQUEST, NO_CONTENT}
import play.api.libs.json.JsObject
import utils.PagerDutyHelper.PagerDutyKeys.FOURXX_RESPONSE_FROM_CONNECTOR

class SelfEmploymentConnectorISpec extends WiremockSpec with IntegrationBaseSpec {

  private def journeyNinoCtx(journey: Journey) = JourneyAnswersWithNino(taxYear, nino, businessId, mtditid, journey)
  private def journeyCtx(journey: Journey)     = JourneyAnswersContext(taxYear, businessId, mtditid, journey)

  private def downstreamNinoUrl(journey: Journey) = s"/income-tax-self-employment/$taxYear/$businessId/$journey/$nino/answers"
  private def downstreamUrl(journey: Journey)     = s"/income-tax-self-employment/$taxYear/$businessId/$journey/answers"

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
      result shouldBe httpError.asLeft
      loggedErrors.exists(_.contains(FOURXX_RESPONSE_FROM_CONNECTOR.toString)) shouldBe true
    }

    "notify pager duty on failure for JourneyAnswersWithNino" in new PagerDutyAware {
      stubPost(url = downstreamNinoUrl(ExpensesGoodsToSellOrUse), BAD_REQUEST)
      val result = connector.submitAnswers[JsObject](journeyNinoCtx(ExpensesGoodsToSellOrUse), JsObject.empty).value.futureValue
      result shouldBe httpError.asLeft
      loggedErrors.exists(_.contains(FOURXX_RESPONSE_FROM_CONNECTOR.toString)) shouldBe true
    }
  }

}
