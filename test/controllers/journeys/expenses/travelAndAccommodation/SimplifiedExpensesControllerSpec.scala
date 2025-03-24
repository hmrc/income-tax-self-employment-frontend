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
import forms.expenses.travelAndAccommodation.SimplifiedExpenseFormProvider
import models.{NormalMode, VehicleType}
import models.common.UserType
import play.api.inject.bind
import models.database.UserAnswers
import navigation.{FakeTravelAndAccommodationNavigator, TravelAndAccommodationNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.travelAndAccommodation.{SimplifiedExpensesPage, TravelForWorkYourVehiclePage, VehicleTypePage}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SelfEmploymentService
import views.html.journeys.expenses.travelAndAccommodation.SimplifiedExpensesView

import scala.concurrent.Future

class SimplifiedExpensesControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider: SimplifiedExpenseFormProvider = new SimplifiedExpenseFormProvider()

  val vehicle: String     = "vehicle"
  val form: Form[Boolean] = formProvider(UserType.Individual, vehicle)

  lazy val onPageLoadRoute: String = routes.SimplifiedExpensesController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val onSubmitRoute: String   = routes.SimplifiedExpensesController.onSubmit(taxYear, businessId, NormalMode).url

  val mockSessionRepository: SessionRepository = mock[SessionRepository]
  val mockService: SelfEmploymentService       = mock[SelfEmploymentService]

  override def beforeEach(): Unit = {
    reset(mockService)
    super.beforeEach()
  }

  "SimplifiedExpenses Controller" - {
    Seq(UserType.Individual, UserType.Agent).foreach { userType =>
      s"when user is $userType" - {
        "must return OK and the correct view for a GET" in {
          val ua = emptyUserAnswers
            .set(TravelForWorkYourVehiclePage, "CarName", Some(businessId))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(ua), userType = userType).build()

          running(application) {
            val request = FakeRequest(GET, onPageLoadRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[SimplifiedExpensesView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, userType, taxYear, businessId, NormalMode, "CarName")(
              request,
              messages(application)).toString
          }
        }

        "must populate the view correctly on a GET when the question has previously been answered" in {

          val userAnswers = UserAnswers(userAnswersId)
            .set(TravelForWorkYourVehiclePage, "CarName", Some(businessId))
            .success
            .value
            .set(VehicleTypePage, VehicleType.values.head, Some(businessId))
            .success
            .value
            .set(SimplifiedExpensesPage, true, Some(businessId))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers), userType = userType).build()

          running(application) {
            val request = FakeRequest(GET, onPageLoadRoute)

            val view = application.injector.instanceOf[SimplifiedExpensesView]

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form.fill(true), userType, taxYear, businessId, NormalMode, "CarName")(
              request,
              messages(application)).toString
          }
        }

        "must redirect to the next page when valid data is submitted" in {

          val userAnswers = emptyUserAnswers
            .set(TravelForWorkYourVehiclePage, "CarName", Some(businessId))
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
              FakeRequest(POST, onPageLoadRoute)
                .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "must return a Bad Request and errors when invalid data is submitted" in {
          val ua = emptyUserAnswers
            .set(TravelForWorkYourVehiclePage, "CarName", Some(businessId))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(ua), userType = userType).build()

          running(application) {
            val request =
              FakeRequest(POST, onPageLoadRoute)
                .withFormUrlEncodedBody(("value", "invalid value"))

            val boundForm = form.bind(Map("value" -> "invalid value"))

            val view = application.injector.instanceOf[SimplifiedExpensesView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, userType, taxYear, businessId, NormalMode, "CarName")(
              request,
              messages(application)).toString
          }
        }
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, onPageLoadRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, onPageLoadRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
