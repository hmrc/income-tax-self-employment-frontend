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
import base.SpecBase.fakeOptionalRequest.userType
import builders.OneColumnSummaryBuilder.testVehicle
import forms.expenses.travelAndAccommodation.AddAnotherVehicleFormProvider
import models.NormalMode
import models.common.UserType
import models.database.UserAnswers
import navigation.{FakeTravelAndAccommodationNavigator, TravelAndAccommodationNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.AddAnotherVehiclePage
import pages.expenses.travelAndAccommodation.TravelForWorkYourVehiclePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.journeys.expenses.travelAndAccommodation.AddAnotherVehicleView

import scala.concurrent.Future

class AddAnotherVehicleControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider: AddAnotherVehicleFormProvider = new AddAnotherVehicleFormProvider()
  val form: Form[Boolean]                         = formProvider(userType)

  lazy val addAnotherVehicleRoute: String =
    routes.AddAnotherVehicleController.onPageLoad(taxYear, businessId, NormalMode).url

  "AddAnotherVehicleController Controller" - {
    Seq(UserType.Individual, UserType.Agent).foreach { userType =>
      s"when user is $userType" - {

        "must return OK and the correct view for a GET" in {
          val userAnswers = UserAnswers(userAnswersId)
            .set(TravelForWorkYourVehiclePage, "Vehicle 1", Some(businessId))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers), userType = userType).build()

          running(application) {
            val request = FakeRequest(GET, addAnotherVehicleRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[AddAnotherVehicleView]

            status(result) mustEqual OK

            contentAsString(result) mustEqual view(form, NormalMode, testVehicle, userType, taxYear, businessId)(
              request,
              messages(application)).toString

          }
        }

        "must populate the view correctly on a GET when the question has previously been answered" in {
          val userAnswers = UserAnswers(userAnswersId)
            .set(TravelForWorkYourVehiclePage, "Vehicle 1", Some(businessId))
            .success
            .value
            .set(AddAnotherVehiclePage, true, Some(businessId))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers), userType = userType).build()

          running(application) {
            val request = FakeRequest(GET, addAnotherVehicleRoute)

            val view = application.injector.instanceOf[AddAnotherVehicleView]

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form.fill(true), NormalMode, testVehicle, userType, taxYear, businessId)(
              request,
              messages(application)).toString

          }
        }

        "must redirect to the next page when valid data is submitted" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[TravelAndAccommodationNavigator].toInstance(new FakeTravelAndAccommodationNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, addAnotherVehicleRoute)
                .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "must return a Bad Request and errors when invalid data is submitted" in {
          val userAnswers = UserAnswers(userAnswersId)
            .set(TravelForWorkYourVehiclePage, "Vehicle 1", Some(businessId))
            .success
            .value
            .set(AddAnotherVehiclePage, true, Some(businessId))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers), userType = userType).build()

          running(application) {
            val request =
              FakeRequest(POST, addAnotherVehicleRoute)
                .withFormUrlEncodedBody(("value", "invalid"))

            val boundForm = form.bind(Map("value" -> "invalid"))

            val view = application.injector.instanceOf[AddAnotherVehicleView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, NormalMode, testVehicle, userType, taxYear, businessId)(
              request,
              messages(application)).toString
          }
        }

        "must redirect to Journey Recovery for a GET if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, addAnotherVehicleRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual
              controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must redirect to Journey Recovery for a POST if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request =
              FakeRequest(POST, addAnotherVehicleRoute)
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
