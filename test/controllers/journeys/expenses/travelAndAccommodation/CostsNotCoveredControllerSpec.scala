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
import forms.standard.CurrencyFormProvider
import models.NormalMode
import models.common.UserType
import models.database.UserAnswers
import navigation.{FakeTravelAndAccommodationNavigator, TravelAndAccommodationNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.CostsNotCoveredPage
import pages.expenses.travelAndAccommodation.VehicleFlatRateChoicePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.journeys.expenses.travelAndAccommodation.CostsNotCoveredView

import scala.concurrent.Future

class CostsNotCoveredControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new CurrencyFormProvider()
  val form: Form[BigDecimal] = formProvider(
    CostsNotCoveredPage,
    userType,
    minValueError = s"costsNotCovered.error.lessThanZero.$userType",
    maxValueError = s"costsNotCovered.error.overMax.$userType",
    nonNumericError = s"costsNotCovered.error.nonNumeric.$userType"
  )

  lazy val costsNotCoveredControllerRoute: String = routes.CostsNotCoveredController.onPageLoad(taxYear, businessId, NormalMode).url

  "CostsNotCoveredController" - {
    Seq(UserType.Individual, UserType.Agent).foreach { userType =>
      s"when user is $userType" - {
        "must return OK and the correct view for a GET" in {
          val ua = emptyUserAnswers
            .set(CostsNotCoveredPage, BigDecimal(25), Some(businessId))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(ua), userType = userType).build()

          val request = FakeRequest(GET, costsNotCoveredControllerRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CostsNotCoveredView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(25), NormalMode, userType, taxYear, businessId)(request, messages(application)).toString
          application.stop()
        }

        "must populate the view correctly on a GET when the question has previously been answered" in {
          val userAnswers = UserAnswers(userAnswersId)
            .set(VehicleFlatRateChoicePage, true, Some(businessId))
            .success
            .value
            .set(CostsNotCoveredPage, BigDecimal(25), Some(businessId))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers), userType = userType).build()

          val request = FakeRequest(GET, costsNotCoveredControllerRoute)

          val view = application.injector.instanceOf[CostsNotCoveredView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(25), NormalMode, userType, taxYear, businessId)(
            request,
            messages(application)
          ).toString

          application.stop()
        }

        "must redirect to the next page when valid data is submitted" in {
          val userAnswers = emptyUserAnswers
            .set(CostsNotCoveredPage, BigDecimal(25), Some(businessId))
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

          val request =
            FakeRequest(POST, costsNotCoveredControllerRoute)
              .withFormUrlEncodedBody(("value", "12"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
          application.stop()
        }

        "must return a Bad Request and errors when invalid data is submitted" in {
          val userAnswers = emptyUserAnswers
            .set(CostsNotCoveredPage, BigDecimal(25))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers), userType = userType).build()

          val request =
            FakeRequest(POST, costsNotCoveredControllerRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST

          application.stop()
        }

        "must redirect to Journey Recovery for a GET if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          val request = FakeRequest(GET, costsNotCoveredControllerRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
          application.stop()
        }

        "must redirect to Journey Recovery for a POST if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          val request =
            FakeRequest(POST, costsNotCoveredControllerRoute)
              .withFormUrlEncodedBody(("value", "answer"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
          application.stop()
        }
      }
    }
  }
}
