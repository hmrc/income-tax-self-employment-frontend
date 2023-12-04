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
import cats.implicits.catsSyntaxEitherId
import connectors.SelfEmploymentConnector
import models.journeys.Journey.Income
import models.journeys.income.{IncomeJourneyAnswers, TradingAllowance}
import org.mockito.ArgumentMatchersSugar
import org.mockito.IdiomaticMockito.StubbingOps
import org.mockito.MockitoSugar.mock
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import scala.concurrent.Future

class SendJourneyAnswersServiceSpec extends SpecBase with ArgumentMatchersSugar {

  private val someJourney = Income

  private val someJourneyAnswers = IncomeJourneyAnswers(
    incomeNotCountedAsTurnover = false,
    nonTurnoverIncomeAmount = None,
    turnoverIncomeAmount = 100.00,
    anyOtherIncome = false,
    otherIncomeAmount = None,
    turnoverNotTaxable = Some(false),
    notTaxableAmount = None,
    tradingAllowance = TradingAllowance.DeclareExpenses,
    howMuchTradingAllowance = None,
    tradingAllowanceAmount = None
  )
  private val mockSEConnector = mock[SelfEmploymentConnector]
  private val service         = new SendJourneyAnswersService(mockSEConnector)

  "sending journey answers" - {
    "connector returns no errors" - {
      "evaluate to unit" in {
        mockSEConnector
          .sendJourneyAnswers(eqTo(submissionContext(someJourney)), eqTo(someJourneyAnswers))(*, *, *) returns Future
          .successful(().asRight)

        service.sendJourneyAnswers(submissionContext(someJourney), someJourneyAnswers).futureValue shouldBe ().asRight
      }
    }
    "connector returns an error" - {
      "return that error" in {
        mockSEConnector
          .sendJourneyAnswers(eqTo(submissionContext(someJourney)), eqTo(someJourneyAnswers))(*, *, *) returns Future
          .successful(httpError.asLeft)

        service.sendJourneyAnswers(submissionContext(someJourney), someJourneyAnswers).futureValue shouldBe httpError.asLeft
      }
    }
  }

}
