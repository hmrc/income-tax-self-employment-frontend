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
import models.Mode
import models.common.Enumerable.Implicits
import models.common.{BusinessId, Enumerable, TaxYear, UserType}
import models.database.UserAnswers
import org.mockito.IdiomaticMockito.StubbingOps
import pages.{OneQuestionPage, QuestionPage}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.{JsString, Writes}
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._

abstract case class RadioButtonGetAndPostQuestionBaseSpec[A: Enumerable](controllerName: String, page: QuestionPage[A])
    extends ControllerSpec
    with Enumerable[A]
    with Implicits {

  def onPageLoadCall: Call
  def onSubmitCall: Call
  def onwardRoute: Call
  def validAnswer: A

  override def withName(str: String): Option[A] = validAnswer.some

  def createForm(userType: UserType): Form[A]

  def expectedView(expectedForm: Form[_], scenario: TestScenario)(implicit request: Request[_], messages: Messages, application: Application): String

  def baseAnswers: UserAnswers = emptyUserAnswersAccrual

  implicit def writes: Writes[A]     = Writes(value => JsString(value.toString))
  def filledUserAnswers: UserAnswers = baseAnswers.set(page, validAnswer, businessId.some)(writes).success.value

  def getRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, onPageLoadCall.url)
  def postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, onSubmitCall.url).withFormUrlEncodedBody(("value", validAnswer.toString))

  mockService
    .submitGatewayQuestionAndRedirect[A](*[OneQuestionPage[A]], *[BusinessId], *[UserAnswers], *, *[TaxYear], *[Mode]) returns Redirect(
    onwardRoute).asFuture

  forAll(userTypeCases) { userType =>
    s"$controllerName for $userType" - {
      val form: Form[A] = createForm(userType)

      "Loading page" - {
        "Redirect to Journey Recover for a GET if prerequisite data is not found" in new TestScenario(userType, None) {
          running(application) {
            val result = route(application, getRequest).value
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual genRoutes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "Return OK for a GET if an empty page" in new TestScenario(userType, Some(baseAnswers)) {
          running(application) {
            val result = route(application, getRequest).value
            status(result) mustEqual OK
            contentAsString(result) mustEqual expectedView(form, this)(getRequest, messages(application), application)
          }
        }

        "Return OK for a GET if an answer to the previous question exists, with the view populated with the previous answer" in new TestScenario(
          userType,
          Some(filledUserAnswers)) {
          running(application) {
            val result = route(application, getRequest).value
            status(result) mustEqual OK
            contentAsString(result) mustEqual expectedView(form.fill(validAnswer), this)(getRequest, messages(application), application)
          }

        }
      }

      "Submitting page" - {
        "Redirect to Journey Recovery for POST if no prerequisite data is found" in new TestScenario(userType, None) {
          running(application) {
            val result = route(application, getRequest).value
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual genRoutes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "Return a Bad Request when invalid data is submitted" in new TestScenario(userType, Some(filledUserAnswers)) {
          running(application) {
            val request   = postRequest.withFormUrlEncodedBody(("value", "invalid value"))
            val result    = route(application, request).value
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
