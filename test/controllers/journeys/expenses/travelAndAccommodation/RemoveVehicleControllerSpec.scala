/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.journeys.expenses.travelAndAccommodation

import base.SpecBase
import forms.expenses.travelAndAccommodation.RemoveVehicleFormProvider
import models.database.UserAnswers
import models.NormalMode
import models.common.UserType
import navigation.{FakeTravelAndAccommodationNavigator, TravelAndAccommodationNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.travelAndAccommodation.{RemoveVehiclePage, TravelForWorkYourVehiclePage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.journeys.expenses.travelAndAccommodation.RemoveVehicleView

import scala.concurrent.Future

class RemoveVehicleControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider    = new RemoveVehicleFormProvider()
  val vehicle: String = "vehicle"

  lazy val removeVehicleRoute: String = routes.RemoveVehicleController.onPageLoad(taxYear, businessId, NormalMode).url

  "RemoveVehicle Controller" - {
    Seq(UserType.Individual, UserType.Agent).foreach { userType =>
      s"when user is $userType" - {
        val form: Form[Boolean] = formProvider(userType, vehicle)

        "must return OK and the correct view for a GET" in {
          val ua = emptyUserAnswers
            .set(TravelForWorkYourVehiclePage, vehicle, Some(businessId))
            .success
            .value
            .set(RemoveVehiclePage, true, Some(businessId))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(ua), userType = userType).build()

          running(application) {
            val request = FakeRequest(GET, removeVehicleRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[RemoveVehicleView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, NormalMode, userType, taxYear, businessId, vehicle)(request, messages(application)).toString
          }
        }

        "must populate the view correctly on a GET when the question has previously been answered" ignore {

          val userAnswers = UserAnswers(userAnswersId).set(RemoveVehiclePage, true, Some(businessId)).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers), userType = userType).build()

          running(application) {
            val request = FakeRequest(GET, removeVehicleRoute)

            val view = application.injector.instanceOf[RemoveVehicleView]

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form.fill(true), NormalMode, userType, taxYear, businessId, vehicle)(
              request,
              messages(application)).toString
          }
        }

        "must redirect to the next page when valid data is submitted" ignore {
          val ua = emptyUserAnswers
            .set(RemoveVehiclePage, true, Some(businessId))
            .success
            .value

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(ua), userType = userType)
              .overrides(
                bind[TravelAndAccommodationNavigator].toInstance(new FakeTravelAndAccommodationNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, removeVehicleRoute)
                .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "must return a Bad Request and errors when invalid data is submitted" ignore {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userType = userType).build()

          running(application) {
            val request =
              FakeRequest(POST, removeVehicleRoute)
                .withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))

            val view = application.injector.instanceOf[RemoveVehicleView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, NormalMode, userType, taxYear, businessId, vehicle)(
              request,
              messages(application)).toString
          }
        }

        "must redirect to Journey Recovery for a GET if no existing data is found" ignore {

          val application = applicationBuilder(userAnswers = None, userType = userType).build()

          running(application) {
            val request = FakeRequest(GET, removeVehicleRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must redirect to Journey Recovery for a POST if no existing data is found" ignore {

          val application = applicationBuilder(userAnswers = None, userType = userType).build()

          running(application) {
            val request =
              FakeRequest(POST, removeVehicleRoute)
                .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
          }
        }
      }
    }
  }
}
