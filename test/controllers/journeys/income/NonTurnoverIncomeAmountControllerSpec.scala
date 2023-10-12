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

package controllers.journeys.income

import base.SpecBase
import controllers.journeys.income.routes.NonTurnoverIncomeAmountController
import controllers.standard.routes.JourneyRecoveryController
import forms.income.NonTurnoverIncomeAmountFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.joda.time.LocalDate
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.NonTurnoverIncomeAmountPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.journeys.income.NonTurnoverIncomeAmountView

import scala.concurrent.Future

class NonTurnoverIncomeAmountControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new NonTurnoverIncomeAmountFormProvider()
  val form = formProvider(isAgentString, tradeName)
  val isAgentString = "individual"
  val tradeName = "tradeName"
  val taxYear = LocalDate.now().getYear

  def onwardRoute = Call("GET", "/foo")

  val validAnswer: BigDecimal = 0

  lazy val nonTurnoverIncomeAmountRoute = NonTurnoverIncomeAmountController.onPageLoad(taxYear, NormalMode).url

  "NonTurnoverIncomeAmount Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, nonTurnoverIncomeAmountRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[NonTurnoverIncomeAmountView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, isAgentString, taxYear)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = UserAnswers(userAnswersId).set(NonTurnoverIncomeAmountPage, validAnswer).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, nonTurnoverIncomeAmountRoute)

          val view = application.injector.instanceOf[NonTurnoverIncomeAmountView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode, isAgentString, taxYear)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" ignore { //TODO unignore when RequireData is implemented

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, nonTurnoverIncomeAmountRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, nonTurnoverIncomeAmountRoute)
              .withFormUrlEncodedBody(("value", validAnswer.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, nonTurnoverIncomeAmountRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))

          val boundForm = form.bind(Map("value" -> "invalid value"))

          val view = application.injector.instanceOf[NonTurnoverIncomeAmountView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, isAgentString, taxYear)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" ignore { //TODO unignore when RequireData is implemented

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, nonTurnoverIncomeAmountRoute)
              .withFormUrlEncodedBody(("value", validAnswer.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
