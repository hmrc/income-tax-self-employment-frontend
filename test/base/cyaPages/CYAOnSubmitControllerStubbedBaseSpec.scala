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

package base.cyaPages

import cats.data.EitherT
import cats.implicits.{catsSyntaxEitherId, catsSyntaxOptionId}
import controllers.{journeys, standard}
import models.NormalMode
import models.common.Journey
import models.common.UserType.Individual
import models.database.UserAnswers
import models.errors.ServiceError
import models.errors.ServiceError.ConnectorResponseError
import org.mockito.IdiomaticMockito.StubbingOps
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.libs.json.JsObject
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{POST, redirectLocation, route, status, writeableOf_AnyContentAsEmpty}
import stubs.services.SelfEmploymentServiceStub

import scala.concurrent.Future

trait CYAOnSubmitControllerStubbedBaseSpec extends CYAControllerBaseSpec {

  protected val journey: Journey
  protected def submissionData: JsObject

  protected implicit lazy val postRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(POST, onSubmitCall(taxYear, businessId).url)

  def userAnswers: UserAnswers = buildUserAnswers(submissionData)

  def stubService      = SelfEmploymentServiceStub()
  def stubServiceError = stubService.copy(submitAnswersResult = ConnectorResponseError("method", "url", httpError).asLeft)

  "submitting a page" - {
    "journey answers submitted successfully" - {
      "redirect to section completed" in new TestStubbedScenario(Individual, userAnswers.some, stubbedService = stubService) {
        val result: Future[Result] = route(application, postRequest).value

        status(result) shouldBe 303

        redirectLocation(result).value shouldBe journeys.routes.SectionCompletedStateController
          .onPageLoad(taxYear, businessId, journey, NormalMode)
          .url
      }
    }

    "an error occurred during answer submission" - {
      "redirect to journey recovery" in new TestStubbedScenario(Individual, userAnswers.some, stubbedService = stubServiceError) {
        val result: Future[Result] = route(application, postRequest).value

        status(result) shouldBe 303
        redirectLocation(result).value shouldBe standard.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
