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
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.requests.DataRequest
import models.{Mode, NormalMode, VehicleType}
import org.mockito.IdiomaticMockito.StubbingOps
import org.mockito.MockitoSugar.mock
import org.mockito.matchers.MacroBasedMatchers
import pages.OneQuestionPage
import pages.expenses.travelAndAccommodation.VehicleTypePage
import play.api.data.{Form, FormBinding}
import play.api.http.Status.SEE_OTHER
import play.api.inject.bind
import play.api.libs.json.Writes
import play.api.mvc.Call
import play.api.mvc.Results.SeeOther
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SelfEmploymentService
import views.html.journeys.expenses.travelAndAccommodation.VehicleTypeView

class VehicleTypeControllerSpec extends SpecBase with MacroBasedMatchers {

  def onwardRoute: Call = Call("GET", "/foo")

  lazy val vehicleTypeRoute: String = routes.VehicleTypeController.onPageLoad(taxYear, businessId, NormalMode).url

  val formProvider            = new VehicleTypeFormProvider()
  val form: Form[VehicleType] = formProvider()

  "VehicleType Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, vehicleTypeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[VehicleTypeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, businessId, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(VehicleTypePage, VehicleType.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, vehicleTypeRoute)

        val view = application.injector.instanceOf[VehicleTypeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(VehicleType.values.head), taxYear, businessId, NormalMode)(
          request,
          messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSelfEmploymentService = mock[SelfEmploymentService]

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SelfEmploymentService].toInstance(mockSelfEmploymentService)
          )
          .build()

      running(application) {

        mockSelfEmploymentService.handleForm(*[Form[_]], *, *)(*[DataRequest[_]], *[FormBinding]) returns SeeOther(onwardRoute.url).asFuture
        mockSelfEmploymentService.defaultHandleForm(*[Form[Any]], *[OneQuestionPage[Any]], *[BusinessId], *[TaxYear], *[Mode], *)(
          *[DataRequest[_]],
          *[FormBinding],
          *[Writes[Any]]
        ) returns SeeOther(onwardRoute.url).asFuture

        val request =
          FakeRequest(POST, vehicleTypeRoute)
            .withFormUrlEncodedBody(("value", VehicleType.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, vehicleTypeRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[VehicleTypeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, businessId, NormalMode)(request, messages(application)).toString
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
