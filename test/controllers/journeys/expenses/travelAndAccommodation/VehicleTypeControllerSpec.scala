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
import forms.VehicleTypeFormProvider
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.requests.DataRequest
import models.{Mode, NormalMode, VehicleType}
import navigation.{FakeTravelAndAccommodationNavigator, TravelAndAccommodationNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.IdiomaticMockito.StubbingOps
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.mockito.matchers.MacroBasedMatchers
import pages.OneQuestionPage
import pages.expenses.travelAndAccommodation.{TravelForWorkYourVehiclePage, VehicleTypePage}
import play.api.data.{Form, FormBinding}
import play.api.http.Status.SEE_OTHER
import play.api.inject.bind
import play.api.libs.json.Writes
import play.api.mvc.Call
import play.api.mvc.Results.SeeOther
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SelfEmploymentService
import views.html.journeys.expenses.travelAndAccommodation.VehicleTypeView

import scala.concurrent.Future

class VehicleTypeControllerSpec extends SpecBase with MacroBasedMatchers {

  def onwardRoute: Call = Call("GET", "/foo")

  lazy val vehicleTypeRoute: String = routes.VehicleTypeController.onPageLoad(taxYear, businessId, NormalMode).url

  val formProvider            = new VehicleTypeFormProvider()
  val form: Form[VehicleType] = formProvider("vehicleName")

  "VehicleType Controller" - {
    Seq(UserType.Individual, UserType.Agent).foreach { userType =>
      s"when user is $userType" - {
        "must return OK and the correct view for a GET" in {
          val ua = emptyUserAnswers
            .set(TravelForWorkYourVehiclePage, "CarName")
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(ua), userType = userType).build()

          running(application) {
            val request = FakeRequest(GET, vehicleTypeRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[VehicleTypeView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, "CarName", taxYear, businessId, NormalMode)(request, messages(application)).toString
          }
        }

        "must populate the view correctly on a GET when the question has previously been answered" in {

          val userAnswers = UserAnswers(userAnswersId)
            .set(TravelForWorkYourVehiclePage, "CarName")
            .success
            .value
            .set(VehicleTypePage, VehicleType.values.head)
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers), userType = userType).build()

          running(application) {
            val request = FakeRequest(GET, vehicleTypeRoute)

            val view = application.injector.instanceOf[VehicleTypeView]

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form.fill(VehicleType.values.head), "CarName", taxYear, businessId, NormalMode)(
              request,
              messages(application)).toString
          }
        }

        "must redirect to the next page when valid data is submitted" in {

          val userAnswers = emptyUserAnswers
            .set(TravelForWorkYourVehiclePage, "CarName")
            .success
            .value
          val mockSessionRepository = mock[SessionRepository]
          when(mockSessionRepository.set(any)).thenReturn(Future.successful(true))

          val application =
            applicationBuilder(userAnswers = Some(userAnswers), userType = userType)
              .overrides(
                bind[TravelAndAccommodationNavigator].toInstance(new FakeTravelAndAccommodationNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {

            val request =
              FakeRequest(POST, vehicleTypeRoute)
                .withFormUrlEncodedBody(("value", VehicleType.values.head.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "must return a Bad Request and errors when invalid data is submitted" in {
          val ua = emptyUserAnswers
            .set(TravelForWorkYourVehiclePage, "CarName")
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(ua), userType = userType).build()

          running(application) {
            val request =
              FakeRequest(POST, vehicleTypeRoute)
                .withFormUrlEncodedBody(("value", "invalid value"))

            val boundForm = form.bind(Map("value" -> "invalid value"))

            val view = application.injector.instanceOf[VehicleTypeView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, "CarName", taxYear, businessId, NormalMode)(request, messages(application)).toString
          }
        }
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, vehicleTypeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, vehicleTypeRoute)
            .withFormUrlEncodedBody(("value", VehicleType.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
