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

package controllers.journeys.expenses

import base.SpecBase
import forms.expenses.AdvertisingOrMarketingFormProvider
import models.journeys.expenses.AdvertisingOrMarketing
import models.NormalMode
import models.database.UserAnswers
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.AdvertisingOrMarketingPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.journeys.expenses.AdvertisingOrMarketingView

import scala.concurrent.Future

class AdvertisingOrMarketingControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val advertisingOrMarketingRoute = controllers.journeys.expenses.routes.AdvertisingOrMarketingController.onPageLoad(NormalMode).url

  val formProvider = new AdvertisingOrMarketingFormProvider()
  val form         = formProvider()

  "AdvertisingOrMarketing Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, advertisingOrMarketingRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AdvertisingOrMarketingView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(AdvertisingOrMarketingPage, AdvertisingOrMarketing.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, advertisingOrMarketingRoute)

        val view = application.injector.instanceOf[AdvertisingOrMarketingView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(AdvertisingOrMarketing.values.head), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, advertisingOrMarketingRoute)
            .withFormUrlEncodedBody(("value", AdvertisingOrMarketing.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, advertisingOrMarketingRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AdvertisingOrMarketingView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" ignore {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, advertisingOrMarketingRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, advertisingOrMarketingRoute)
            .withFormUrlEncodedBody(("value", AdvertisingOrMarketing.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
