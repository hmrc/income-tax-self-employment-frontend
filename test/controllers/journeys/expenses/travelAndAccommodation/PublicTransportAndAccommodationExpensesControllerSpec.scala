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
import forms.standard.CurrencyFormProvider
import models.common.{BusinessId, TaxYear, UserType}
import models.{Mode, NormalMode}
import navigation.{FakeTravelAndAccommodationNavigator, TravelAndAccommodationNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.travelAndAccommodation.PublicTransportAndAccommodationExpensesPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.journeys.expenses.travelAndAccommodation.PublicTransportAndAccommodationExpensesView

import scala.concurrent.Future

class PublicTransportAndAccommodationExpensesControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute  = Call("GET", "/foo")
  private val mode = NormalMode

  val validAnswer = BigDecimal(89.55)

  private def onPageLoadRoute(taxYear: TaxYear, businessId: BusinessId, mode: Mode): String =
    controllers.journeys.expenses.travelAndAccommodation.routes.PublicTransportAndAccommodationExpensesController
      .onPageLoad(taxYear, businessId, mode)
      .url

  "PublicTransportAndAccommodationExpenses Controller" - {
    Seq(UserType.Individual, UserType.Agent).foreach { userType =>
      val formProvider = new CurrencyFormProvider()
      val form: Form[BigDecimal] =
        formProvider(PublicTransportAndAccommodationExpensesPage, userType, prefix = Some("publicTransportAndAccommodationExpenses"))

      s"when user is $userType" - {
        "must return OK and the correct view for a GET" in {

          val ua          = emptyUserAnswers
          val application = applicationBuilder(userAnswers = Some(ua), userType = userType).build()
          val request     = FakeRequest(GET, onPageLoadRoute(taxYear, businessId, mode))

          running(application) {
            val result = route(application, request).value
            val view   = application.injector.instanceOf[PublicTransportAndAccommodationExpensesView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, mode, userType, taxYear, businessId)(request, messages(application)).toString
          }
        }

        "must populate the view correctly on a GET when the question has previously been answered" in {

          val userAnswers = emptyUserAnswers
            .set(PublicTransportAndAccommodationExpensesPage, validAnswer, Option(businessId))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers), userType = userType).build()

          running(application) {
            val request = FakeRequest(GET, onPageLoadRoute(taxYear, businessId, mode))
            val view =
              application.injector.instanceOf[PublicTransportAndAccommodationExpensesView]

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form.fill(validAnswer), mode, userType, taxYear, businessId)(
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
              FakeRequest(POST, onPageLoadRoute(taxYear, businessId, mode))
                .withFormUrlEncodedBody(("value", validAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "must return a Bad Request and errors when invalid data is submitted" in {
          val userAnswers = emptyUserAnswers
            .set(PublicTransportAndAccommodationExpensesPage, BigDecimal(-20), Option(businessId))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers), userType = userType).build()

          running(application) {
            val request = FakeRequest(POST, onPageLoadRoute(taxYear, businessId, mode))

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
          }
        }

        "must redirect to Journey Recovery for a GET if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, onPageLoadRoute(taxYear, businessId, mode))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must redirect to Journey Recovery for a POST if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request =
              FakeRequest(POST, onPageLoadRoute(taxYear, businessId, mode))
                .withFormUrlEncodedBody(("value", validAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
          }
        }
      }
    }
  }
}
