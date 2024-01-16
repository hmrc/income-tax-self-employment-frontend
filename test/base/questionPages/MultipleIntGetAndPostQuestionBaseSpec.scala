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

package base.questionPages

import base.ControllerSpec
import cats.implicits.catsSyntaxOptionId
import controllers.standard.{routes => genRoutes}
import models.common.{BusinessId, UserType}
import models.database.UserAnswers
import org.mockito.IdiomaticMockito.StubbingOps
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.Page
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsFormUrlEncoded, Call, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._

abstract case class MultipleIntGetAndPostQuestionBaseSpec[A](controller: String, page: Page) extends ControllerSpec {

  val amount: Int = 2

  /** Implementers should avoid eager overrides of below.
    */
  def onPageLoadRoute: String
  def onSubmitRoute: String

  def onwardRoute: Call

  /** Implementers can provide prerequisite answers the controller requires.
    */
  def baseAnswers: UserAnswers = emptyUserAnswers

  def pageAnswers: UserAnswers

  def createForm(userType: UserType): Form[A]
  def validFormModel: A

  def expectedView(expectedForm: Form[A], scenario: TestScenario)(implicit request: Request[_], messages: Messages, application: Application): String

  mockService.persistAnswer(*[BusinessId], *[UserAnswers], *, *)(*) returns pageAnswers.asFuture

  private lazy val getRequest = FakeRequest(GET, onPageLoadRoute)
  def postRequest: FakeRequest[AnyContentAsFormUrlEncoded]

  forAll(userTypeCases) { user =>
    s"$controller for $user" - {
      val form: Form[A] = createForm(user)

      "on page load" - {
        "answers exist for the page" - {
          "return Ok and the view with the existing answer" in new TestScenario(user, answers = pageAnswers.some) {
            running(application) {
              val result = route(application, getRequest).value

              status(result) shouldBe OK
              contentAsString(result) shouldBe expectedView(form.fill(validFormModel), this)(getRequest, messages(application), application)
            }
          }
        }
        "the page has no existing answers" - {
          "return Ok" in new TestScenario(user, answers = baseAnswers.some) {
            running(application) {
              val result = route(application, getRequest).value

              status(result) shouldBe OK
              contentAsString(result) shouldBe expectedView(form, this)(getRequest, messages(application), application)
            }
          }
        }
        // Below test for checking `requireData` is invoked.
        "no answers exist in the session" - {
          "redirect to the journey recovery controller" in new TestScenario(user, answers = None) {
            running(application) {
              val result = route(application, getRequest).value

              status(result) shouldBe SEE_OTHER
              redirectLocation(result).value shouldBe genRoutes.JourneyRecoveryController.onPageLoad().url
            }
          }
        }
      }

      "on page submission" - {
        "valid data is submitted" - {
          "redirect to the next page" in new TestScenario(user, answers = pageAnswers.some) {
            running(application) {
              val result = route(application, postRequest).value

              status(result) shouldBe SEE_OTHER
              redirectLocation(result).value shouldBe onwardRoute.url
            }
          }
        }
        "invalid data is submitted" - {
          "return a 400 and pass the errors to the view" in new TestScenario(user, answers = baseAnswers.some) {
            running(application) {
              val request   = postRequest.withFormUrlEncodedBody(("value", "invalid value"))
              val result    = route(application, request).value
              val boundForm = createForm(userType).bind(Map("value" -> "invalid value"))

              status(result) shouldBe BAD_REQUEST
              contentAsString(result) shouldBe expectedView(boundForm, this)(request, messages(application), application)
            }
          }
        }
        "no answers exist in the session" - {
          "Redirect to the journey recovery page" in new TestScenario(user, answers = None) {
            running(application) {
              val result = route(application, getRequest).value

              status(result) shouldBe SEE_OTHER
              redirectLocation(result).value shouldBe genRoutes.JourneyRecoveryController.onPageLoad().url
            }
          }
        }
      }
    }
  }
}
