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
import forms.expenses.travelAndAccommodation.VehicleExpensesFormProvider
import models.{NormalMode, VehicleType}
import models.common.UserType
import models.database.UserAnswers
import models.journeys.expenses.travelAndAccommodation.TravelAndAccommodationExpenseType
import navigation.{FakeTravelAndAccommodationNavigator, TravelAndAccommodationNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.travelAndAccommodation._
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.journeys.expenses.travelAndAccommodation.VehicleExpensesView

import scala.concurrent.Future

class VehicleExpensesControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider           = new VehicleExpensesFormProvider()
  val form: Form[BigDecimal] = formProvider(userType)

  lazy val vehicleExpensesControllerRoute: String = routes.VehicleExpensesController.onPageLoad(taxYear, businessId, NormalMode).url

  "VehicleType Controller" - {
    Seq(UserType.Individual, UserType.Agent).foreach { userType =>
      s"when user is $userType" - {
        "must return OK and the correct view for a GET" in {
          val travelExpenseAnswer: Set[TravelAndAccommodationExpenseType] =
            Set(TravelAndAccommodationExpenseType.LeasedVehicles, TravelAndAccommodationExpenseType.MyOwnVehicle)
          val ua = emptyUserAnswers
            .set(TravelAndAccommodationExpenseTypePage, travelExpenseAnswer, Some(businessId))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(ua), userType = userType).build()

          running(application) {
            val request = FakeRequest(GET, vehicleExpensesControllerRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[VehicleExpensesView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, NormalMode, userType, taxYear, businessId, travelExpenseAnswer)(
              request,
              messages(application)).toString
          }
        }

        "must populate the view correctly on a GET when the question has previously been answered" in {
          val travelExpenseAnswer: Set[TravelAndAccommodationExpenseType] =
            Set(TravelAndAccommodationExpenseType.LeasedVehicles)

          val userAnswers = UserAnswers(userAnswersId)
            .set(TravelAndAccommodationExpenseTypePage, travelExpenseAnswer, Some(businessId))
            .success
            .value
            .set(TravelForWorkYourVehiclePage, "CarName", Some(businessId))
            .success
            .value
            .set(VehicleTypePage, VehicleType.values.head, Some(businessId))
            .success
            .value
            .set(SimplifiedExpensesPage, true, Some(businessId))
            .success
            .value
            .set(VehicleExpensesPage, BigDecimal(25), Some(businessId))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers), userType = userType).build()

          running(application) {
            val request = FakeRequest(GET, vehicleExpensesControllerRoute)

            val view = application.injector.instanceOf[VehicleExpensesView]

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form.fill(25), NormalMode, userType, taxYear, businessId, travelExpenseAnswer)(
              request,
              messages(application)
            ).toString

          }
        }

        "must redirect to the next page when valid data is submitted" in {
          val travelExpenseAnswer: Set[TravelAndAccommodationExpenseType] =
            Set(TravelAndAccommodationExpenseType.LeasedVehicles)

          val userAnswers = emptyUserAnswers
            .set(TravelAndAccommodationExpenseTypePage, travelExpenseAnswer)
            .success
            .value
            .set(VehicleExpensesPage, BigDecimal(25))
            .success
            .value

          val mockSessionRepository = mock[SessionRepository]
          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(userAnswers), userType = userType)
              .overrides(
                bind[TravelAndAccommodationNavigator].toInstance(new FakeTravelAndAccommodationNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, vehicleExpensesControllerRoute)
                .withFormUrlEncodedBody(("value", "12"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "must return a Bad Request and errors when invalid data is submitted" in {
          val travelExpenseAnswer: Set[TravelAndAccommodationExpenseType] =
            Set(TravelAndAccommodationExpenseType.LeasedVehicles)

          val userAnswers = emptyUserAnswers
            .set(TravelAndAccommodationExpenseTypePage, travelExpenseAnswer)
            .success
            .value
            .set(VehicleExpensesPage, BigDecimal(25))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers), userType = userType).build()

          running(application) {
            val request =
              FakeRequest(POST, vehicleExpensesControllerRoute)
                .withFormUrlEncodedBody(("value", "invalid value"))

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST

          }
        }

        "must redirect to Journey Recovery for a GET if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, vehicleExpensesControllerRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must redirect to Journey Recovery for a POST if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request =
              FakeRequest(POST, vehicleExpensesControllerRoute)
                .withFormUrlEncodedBody(("value", "answer"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
          }
        }
      }
    }
  }
}
