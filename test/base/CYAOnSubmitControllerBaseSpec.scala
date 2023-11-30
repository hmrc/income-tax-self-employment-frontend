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

package base

import cats.implicits.{catsSyntaxEitherId, catsSyntaxOptionId}
import controllers.journeys.routes._
import controllers.standard.routes._
import models.NormalMode
import models.common.UserType.Individual
import models.database.UserAnswers
import models.journeys.Journey
import org.mockito.IdiomaticMockito.StubbingOps
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.inject.{Binding, bind}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{POST, defaultAwaitTimeout, redirectLocation, route, status, writeableOf_AnyContentAsEmpty}
import services.SendJourneyAnswersService

import scala.concurrent.Future

trait CYAOnSubmitControllerBaseSpec[T] extends ControllerSpec {

  protected val userAnswers: UserAnswers
  protected val journeyAnswers: T
  protected val journey: Journey
  protected val onSubmitRoute: String

  private val mockService: SendJourneyAnswersService = mock[SendJourneyAnswersService]

  override val bindings: List[Binding[_]] = List(bind[SendJourneyAnswersService].toInstance(mockService))

  protected implicit lazy val postRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(POST, onSubmitRoute)

  "submitting a page" - {
    "journey answers submitted successfully" - {
      "redirect to section completed" in new TestScenario(Individual, userAnswers.some) {
        mockService
          .sendJourneyAnswers(eqTo(testScenarioContext(journey)), eqTo(journeyAnswers))(*, *, *) returns Future.successful(().asRight)

        val result: Future[Result] = route(application, postRequest).value

        status(result) shouldBe 303

        redirectLocation(result).value shouldBe SectionCompletedStateController
          .onPageLoad(taxYear, businessId, journey.toString, NormalMode)
          .url

      }
    }
    "an error occurred during answer submission" - {
      "redirect to journey recovery" in new TestScenario(Individual, userAnswers.some) {
        mockService
          .sendJourneyAnswers(eqTo(testScenarioContext(journey)), eqTo(journeyAnswers))(*, *, *) returns Future.successful(httpError.asLeft)

        val result: Future[Result] = route(application, postRequest).value

        status(result) shouldBe 303
        redirectLocation(result).value shouldBe JourneyRecoveryController.onPageLoad().url

      }
    }
  }

}