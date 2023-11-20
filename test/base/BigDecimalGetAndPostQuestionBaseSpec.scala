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

import controllers.standard.{routes => genRoutes}
import models.common.{UserType, onwardRoute}
import models.database.UserAnswers
import pages.QuestionPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers._

abstract case class BigDecimalGetAndPostQuestionBaseSpec(
    controllerName: String,
    page: QuestionPage[BigDecimal]
) extends ControllerSpec {
  val onPageLoadRoute: String
  val onSubmitRoute: String

  def createForm(userType: UserType): Form[BigDecimal]

  def expectedView(expectedForm: Form[_], scenario: TestScenario)(implicit request: Request[_], messages: Messages, application: Application): String

  val validAnswer: BigDecimal        = 100.00
  val filledUserAnswers: UserAnswers = UserAnswers(userAnswersId).set(page, validAnswer, Some(stubbedBusinessId)).success.value

  def getRequest  = FakeRequest(GET, onPageLoadRoute)
  def postRequest = FakeRequest(POST, onSubmitRoute).withFormUrlEncodedBody(("value", validAnswer.toString))

  forAll(langUserTypeCases) { case (lang, userType) =>
    s"$controllerName for ${lang.entryName} and ${userType}" - {
      val form: Form[BigDecimal] = createForm(userType)

      "Loading page" - {
        "Redirect to Journey Recover for a GET if prerequisite data is not found" in new TestScenario(userType, None) {
          running(application) {
            val result = route(application, getRequest).value
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual genRoutes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "Return OK for a GET if an empty page" in new TestScenario(userType, Some(emptyUserAnswers)) {
          running(application) {
            val response = route(application, getRequest).value
            val result   = response.map(languageAwareResult(lang, _))
            status(result) mustEqual OK
            contentAsString(result) mustEqual expectedView(form, this)(getRequest, messages(application), application)
          }
        }

        "Return OK for a GET if an answer to the previous question exists, with the view populated with the previous answer" in new TestScenario(
          userType,
          Some(filledUserAnswers)) {
          running(application) {
            val response = route(application, getRequest).value
            val result   = response.map(languageAwareResult(lang, _))
            status(result) mustEqual OK
            contentAsString(result) mustEqual expectedView(form.fill(validAnswer), this)(getRequest, messages(application), application)
          }

        }
      }

      "Submitting page" - {
        "Redirect to Journey Recovery for POST if no prerequisite data is found" in new TestScenario(userType, None) {
          running(application) {
            val response = route(application, postRequest).value
            val result   = response.map(languageAwareResult(lang, _))
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual genRoutes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "Return a Bad Request when invalid data is submitted" in new TestScenario(userType, Some(emptyUserAnswers)) {
          running(application) {
            val request   = postRequest.withFormUrlEncodedBody(("value", "invalid value"))
            val response  = route(application, request).value
            val result    = response.map(languageAwareResult(lang, _))
            val boundForm = createForm(userType).bind(Map("value" -> "invalid value"))
            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual expectedView(boundForm, this)(request, messages(application), application)
          }
        }

        "Redirect to the next page on submit" in new TestScenario(userType, Some(filledUserAnswers)) {
          running(application) {
            val result = route(application, postRequest).value
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

      }
    }
  }

}
