/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers

import base.ControllerTestScenarioSpec
import base.SpecBase.ToFutureOps
import cats.implicits.catsSyntaxOptionId
import models.Mode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.requests.DataRequest
import org.mockito.IdiomaticMockito.StubbingOps
import org.scalatest.OptionValues._
import org.scalatest.wordspec.AnyWordSpecLike
import pages.OneQuestionPage
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, PlayRunners}

trait StandardControllerSpec extends AnyWordSpecLike with PlayRunners with ControllerTestScenarioSpec {
  def onwardUrl = "onwardRoute"

  def checkOnPageLoad(path: => String, userAnswer: UserAnswers, expectedTitle: String): Unit = {
    def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, path)

    "onPageLoad" should {

      "render the correct view" in new TestScenario(
        UserType.Individual,
        userAnswer.some
      ) {
        running(application) {
          val result = route(application, request).value
          assert(status(result) === OK)
          assert(getTitle(result) === s"$expectedTitle - income-tax-self-employment-frontend - GOV.UK")
        }
      }
    }
  }

  def checkOnSubmit(path: => String, userAnswers: UserAnswers, formData: (String, String)*) =
    "onSubmit" should {
      def request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, path)
        .withFormUrlEncodedBody(formData: _*)

      mockService.persistAnswer(*[BusinessId], *[UserAnswers], *, *)(*) returns userAnswers.asFuture
      mockService.persistAnswerAndRedirect(*[OneQuestionPage[_]], *[BusinessId], *[DataRequest[_]], *, *[TaxYear], *[Mode])(*) returns Redirect(
        onwardUrl).asFuture
      mockService.submitGatewayQuestionAndRedirect(
        *[OneQuestionPage[Boolean]],
        *[BusinessId],
        *[UserAnswers],
        *,
        *[TaxYear],
        *[Mode]) returns Redirect(onwardUrl).asFuture

      "render the correct view" in new TestScenario(
        UserType.Individual,
        userAnswers.some
      ) {
        running(application) {
          val result = route(application, request).value
          assert(status(result) === SEE_OTHER)
          assert(redirectLocation(result).value === onwardUrl)
        }
      }
    }
}
