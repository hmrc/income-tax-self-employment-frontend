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
import models.journeys.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status.BAD_REQUEST
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

import scala.concurrent.Future

class ExpensesServiceSpec extends SpecBase {

  private val journeyAnswers = GoodsToSellOrUseJourneyAnswers(goodsToSellOrUseAmount = 100.00, disallowableGoodsToSellOrUseAmount = Some(100.00))

  private val mtditid = "someId"

  // Below is common so move out
  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

  private val mockSEConnector = mock[SelfEmploymentConnector]
  private val service         = new ExpensesService(mockSEConnector)

  // Pull out this error if it is commonly used elsewhere
  private val httpError = HttpError(BAD_REQUEST, SingleErrorBody("PARSING_ERROR", "Error parsing response from CONNECTOR"))

  "ExpensesService" - {
    "sending expenses answers" - {
      "connector returns no errors" - {
        "evaluate to unit" in {
          when(
            mockSEConnector
              .sendExpensesAnswers(eqTo(currTaxYear), eqTo(stubBusinessId), eqTo(someNino), eqTo(mtditid), eqTo(journeyAnswers))(any(), any(), any()))
            .thenReturn(Future.successful(().asRight))

          service.sendExpensesAnswers(currTaxYear, stubBusinessId, someNino, mtditid, journeyAnswers).futureValue shouldBe ().asRight
        }
      }
      "connector returns an error" - {
        "return that error" in {
          when(
            mockSEConnector
              .sendExpensesAnswers(eqTo(currTaxYear), eqTo(stubBusinessId), eqTo(someNino), eqTo(mtditid), eqTo(journeyAnswers))(any(), any(), any()))
            .thenReturn(Future.successful(httpError.asLeft))

          service.sendExpensesAnswers(currTaxYear, stubBusinessId, someNino, mtditid, journeyAnswers).futureValue shouldBe httpError.asLeft
        }
      }
    }
  }

}
