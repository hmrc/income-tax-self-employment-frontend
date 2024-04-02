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
import forms.standard.LocalDateFormProvider
import models.common.{BusinessId, UserType}
import models.database.UserAnswers
import org.mockito.IdiomaticMockito.StubbingOps
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.OneQuestionPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import java.time.LocalDate

abstract case class LocalDateGetAndPostQuestionBaseSpec(controller: String, page: OneQuestionPage[LocalDate]) extends ControllerSpec {

  def validDate: LocalDate = LocalDate.now()
  val form                 = new LocalDateFormProvider

  /** Implementers should avoid eager overrides of below.
    */
  def onPageLoadRoute: String
  def onSubmitRoute: String

  def onwardRoute: Call

  /** Implementers can provide prerequisite answers the controller requires.
    */
  def baseAnswers: UserAnswers = emptyUserAnswersAccrual

  def pageAnswers: UserAnswers = baseAnswers.set(page, validDate, businessId.some).success.value

  def createForm(userType: UserType): Form[LocalDate] = form(page, userType)

  def expectedView(expectedForm: Form[_], scenario: TestScenario)(implicit request: Request[_], messages: Messages, application: Application): String

  mockService.persistAnswer(*[BusinessId], *[UserAnswers], *, *)(*) returns pageAnswers.asFuture

  private def getRequest = FakeRequest(GET, onPageLoadRoute)
  private def postRequest = FakeRequest(POST, onSubmitRoute).withFormUrlEncodedBody(
    (s"${page.toString}.day", validDate.getDayOfMonth.toString),
    (s"${page.toString}.month", validDate.getMonthValue.toString),
    (s"${page.toString}.year", validDate.getYear.toString)
  )

  forAll(userTypeCases) { user =>
    s"$controller for $user" - {
      val form = createForm(user)

      "on page load" - {
        "answers exist for the page" - {
          "return Ok and the view with the existing answer" in new TestScenario(user, answers = pageAnswers.some) {
            running(application) {
              val result = route(application, getRequest).value

              status(result) shouldBe OK
              contentAsString(result) shouldBe expectedView(form.fill(validDate), this)(getRequest, messages(application), application)
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
              assert(redirectLocation(result).value.endsWith(onwardRoute.url))
            }
          }
        }
        "invalid data is submitted" - {
          "return a 400 and pass the errors to the view" in new TestScenario(user, answers = baseAnswers.some) {
            running(application) {
              val request   = postRequest.withFormUrlEncodedBody((page.toString, "invalid value"))
              val result    = route(application, request).value
              val boundForm = createForm(userType).bind(Map(page.toString -> "invalid value"))

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
