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
import forms.standard.CurrencyFormProvider
import models.Mode
import models.common.UserType.Individual
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.requests.DataRequest
import org.mockito.IdiomaticMockito.StubbingOps
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.OneQuestionPage
import play.api.Application
import play.api.data.{Form, FormBinding}
import play.api.i18n.Messages
import play.api.libs.json.Writes
import play.api.mvc.Results.{BadRequest, Redirect}
import play.api.mvc.{AnyContentAsFormUrlEncoded, Call, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._

abstract case class BigDecimalGetAndPostQuestionBaseSpec(controller: String, page: OneQuestionPage[BigDecimal]) extends ControllerSpec {

  val amount: BigDecimal = BigDecimal(100.00)
  val form               = new CurrencyFormProvider

  /** Implementers should avoid eager overrides of below.
    */
  def onPageLoadRoute: String
  def onSubmitRoute: String

  def onwardRoute: Call

  /** Implementers can provide prerequisite answers the controller requires.
    */
  def baseAnswers: UserAnswers = emptyUserAnswersAccrual

  def pageAnswers: UserAnswers = baseAnswers.set(page, amount, businessId.some).success.value

  def createForm(userType: UserType): Form[BigDecimal] = form(page, userType, prefix = page.toString.some)

  def expectedView(expectedForm: Form[_], scenario: TestScenario)(implicit request: Request[_], messages: Messages, application: Application): String

  mockService.persistAnswer(*[BusinessId], *[UserAnswers], *, *)(*) returns pageAnswers.asFuture

  private def getRequest  = FakeRequest(GET, onPageLoadRoute)
  private def postRequest = FakeRequest(POST, onSubmitRoute).withFormUrlEncodedBody(("value", amount.toString))

  controller - {
    def form(userType: UserType = Individual): Form[BigDecimal] = createForm(userType)

    "on page load" - {
      "answers exist for the page" - {
        "return Ok and the view with the existing answer" in new TestScenario(answers = pageAnswers.some) {
          running(application) {
            val result = route(application, getRequest).value

            status(result) shouldBe OK
            contentAsString(result) shouldBe expectedView(form().fill(amount), this)(getRequest, messages(application), application)
          }
        }
      }
      "the page has no existing answers" - {
        forAll(userTypeCases) { user =>
          s"when user is $user, return Ok" in new TestScenario(user, answers = baseAnswers.some) {
            running(application) {
              val result = route(application, getRequest).value

              status(result) shouldBe OK
              contentAsString(result) shouldBe expectedView(form(user), this)(getRequest, messages(application), application)
            }
          }
        }
      }
      // Below test for checking `requireData` is invoked.
      "no answers exist in the session" - {
        "redirect to the journey recovery controller" in new TestScenario(answers = None) {
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
        "redirect to the next page" in new TestScenario(answers = pageAnswers.some) {
          mockService.defaultHandleForm(*[Form[BigDecimal]], *[OneQuestionPage[BigDecimal]], *[BusinessId], *[TaxYear], *[Mode], *)(
            *[DataRequest[_]],
            *[FormBinding],
            *[Writes[BigDecimal]]
          ) returns Redirect(onwardRoute).asFuture

          running(application) {
            val result                     = route(application, postRequest).value
            val redirectMatchesOnwardRoute = redirectLocation(result).value.endsWith(onwardRoute.url)

            status(result) shouldBe SEE_OTHER
            assert(redirectMatchesOnwardRoute)
          }
        }
      }
      "invalid data is submitted" - {
        forAll(userTypeCases) { user =>
          s"when user is $user, return a 400 and pass the errors to the view" in new TestScenario(user, answers = baseAnswers.some) {
            val request: FakeRequest[AnyContentAsFormUrlEncoded] = postRequest.withFormUrlEncodedBody(("value", "invalid value"))
            val boundForm: Form[BigDecimal]                      = createForm(userType).bind(Map("value" -> "invalid value"))
            val expectedErrorView: String                        = expectedView(boundForm, this)(request, messages(application), application)

            mockService.defaultHandleForm(*[Form[BigDecimal]], *[OneQuestionPage[BigDecimal]], *[BusinessId], *[TaxYear], *[Mode], *)(
              *[DataRequest[_]],
              *[FormBinding],
              *[Writes[BigDecimal]]
            ) returns BadRequest(expectedErrorView).asFuture

            running(application) {
              val result = route(application, request).value

              status(result) shouldBe BAD_REQUEST
              contentAsString(result) shouldBe expectedErrorView
            }
          }
        }
      }
      "no answers exist in the session" - {
        "Redirect to the journey recovery page" in new TestScenario(answers = None) {
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
