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

package services.journeys.expenses

import base.SpecBase
import cats.implicits.catsSyntaxEitherId
import connectors.SelfEmploymentConnector
import models.errors.HttpError
import models.errors.HttpErrorBody.SingleErrorBody
import models.journeys.Journey.ExpensesGoodsToSellOrUse
import models.journeys.expenses.ExpensesData
import models.journeys.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import org.mockito.ArgumentMatchersSugar
import org.mockito.IdiomaticMockito.StubbingOps
import org.mockito.MockitoSugar.mock
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status.BAD_REQUEST

import scala.concurrent.Future

class ExpensesServiceSpec extends SpecBase with ArgumentMatchersSugar {

  private val someExpensesJourney = ExpensesGoodsToSellOrUse
  private val someExpensesData    = ExpensesData(taxYear, someNino, stubBusinessId, someExpensesJourney, mtditid)

  private val someExpensesAnswers = GoodsToSellOrUseJourneyAnswers(goodsToSellOrUseAmount = 100.00, disallowableGoodsToSellOrUseAmount = Some(100.00))

  private val mockSEConnector = mock[SelfEmploymentConnector]
  private val service         = new ExpensesService(mockSEConnector)

  private val httpError = HttpError(BAD_REQUEST, SingleErrorBody("PARSING_ERROR", "Error parsing response from CONNECTOR"))

  "sending expenses answers" - {
    "connector returns no errors" - {
      "evaluate to unit" in {
        mockSEConnector
          .sendExpensesAnswers(*[ExpensesData], *[GoodsToSellOrUseJourneyAnswers])(*, *, *) returns Future
          .successful(().asRight)

        service.sendExpensesAnswers(someExpensesData, someExpensesAnswers).futureValue shouldBe ().asRight
      }
    }
    "connector returns an error" - {
      "return that error" in {
        mockSEConnector
          .sendExpensesAnswers(*[ExpensesData], *[GoodsToSellOrUseJourneyAnswers])(*, *, *) returns Future
          .successful(httpError.asLeft)

        service.sendExpensesAnswers(someExpensesData, someExpensesAnswers).futureValue shouldBe httpError.asLeft
      }
    }
  }

}
