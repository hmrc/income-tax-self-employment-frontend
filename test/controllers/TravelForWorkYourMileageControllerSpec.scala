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

package controllers

import base.SpecBase
import controllers.journeys.expenses.travelAndAccommodation.routes.TravelForWorkYourMileageController
import forms.TravelForWorkYourMileageFormProvider
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.{CheckMode, Mode, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.travelAndAccommodation.{TravelForWorkYourMileagePage, TravelForWorkYourVehiclePage}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.journeys.expenses.travelAndAccommodation.TravelForWorkYourMileageView

import scala.concurrent.Future

class TravelForWorkYourMileageControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new TravelForWorkYourMileageFormProvider()

  def onwardRoute = Call("GET", "/foo")

  val mileage = 300
  val vehicle = "Grey Astra"

  case class UserScenario(userType: UserType, form: Form[Int])

  private val userScenarios = Seq(
    UserScenario(UserType.Individual, formProvider(UserType.Individual, vehicle)),
    UserScenario(UserType.Agent, formProvider(UserType.Agent, vehicle))
  )

  private def onPageLoadRoute(taxYear: TaxYear, businessId: BusinessId, mode: Mode): String =
    TravelForWorkYourMileageController.onPageLoad(taxYear, businessId, mode).url

  private def onPageSubmitRoute(taxYear: TaxYear, businessId: BusinessId, mode: Mode): String =
    TravelForWorkYourMileageController.onSubmit(taxYear, businessId, mode).url

  "TravelForWorkYourMileage Controller" - {
    userScenarios.foreach { scenario =>
      s"when user is ${scenario.userType}" - {
        "onPageLoad" - {
          "must return OK and the correct view for a GET" in {

            val ua = emptyUserAnswers
              .set(TravelForWorkYourVehiclePage, vehicle, Some(businessId))
              .success
              .value

            val application = applicationBuilder(userAnswers = Some(ua), userType = scenario.userType).build()

            running(application) {
              val request = FakeRequest(GET, onPageLoadRoute(taxYear, businessId, NormalMode))
              val result  = route(application, request).value
              val view    = application.injector.instanceOf[TravelForWorkYourMileageView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(
                scenario.form,
                NormalMode,
                scenario.userType,
                taxYear,
                businessId,
                vehicle
              )(request, messages(application)).toString
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {
            val userAnswers = UserAnswers(userAnswersId)
              .set(TravelForWorkYourVehiclePage, vehicle, Some(businessId))
              .flatMap(_.set(TravelForWorkYourMileagePage, mileage, Some(businessId)))
              .success
              .value

            val application = applicationBuilder(userAnswers = Some(userAnswers), userType = scenario.userType).build()

            running(application) {
              val request = FakeRequest(GET, onPageLoadRoute(taxYear, businessId, CheckMode))
              val view    = application.injector.instanceOf[TravelForWorkYourMileageView]
              val result  = route(application, request).value

              val expectedForm = new TravelForWorkYourMileageFormProvider()(scenario.userType, vehicle).fill(mileage)

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(
                expectedForm,
                CheckMode,
                scenario.userType,
                taxYear,
                businessId,
                vehicle
              )(request, messages(application)).toString
            }
          }
        }

        "onSubmit" - {
          "must redirect to the next page when valid data is submitted" in {
            val userAnswers = UserAnswers(userAnswersId)
              .set(TravelForWorkYourVehiclePage, vehicle, Some(businessId))
              .success
              .value

            val mockSessionRepository = mock[SessionRepository]
            when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

            val application =
              applicationBuilder(userAnswers = Some(userAnswers), userType = scenario.userType)
                .build()

            running(application) {
              val request =
                FakeRequest(POST, onPageSubmitRoute(taxYear, businessId, NormalMode))
                  .withFormUrlEncodedBody(("value", mileage.toString))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
            }
          }

          "must return a Bad Request and errors when invalid data is submitted" in {
            val userAnswers = emptyUserAnswers
              .set(TravelForWorkYourVehiclePage, vehicle, Some(businessId))
              .success
              .value

            val application = applicationBuilder(userAnswers = Some(userAnswers), userType = scenario.userType).build()

            running(application) {
              val request =
                FakeRequest(POST, onPageSubmitRoute(taxYear, businessId, NormalMode))
                  .withFormUrlEncodedBody(("value", "invalid value"))

              val formProvider = application.injector.instanceOf[TravelForWorkYourMileageFormProvider]
              val form         = formProvider(scenario.userType, vehicle)
              val boundForm    = form.bind(Map("value" -> "invalid value"))

              val view = application.injector.instanceOf[TravelForWorkYourMileageView]

              val result = route(application, request).value

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual view(boundForm, NormalMode, scenario.userType, taxYear, businessId, vehicle)(
                request,
                messages(application)).toString
            }
          }

          "must redirect to Journey Recovery for a GET if no existing data is found" in {

            val application = applicationBuilder(userAnswers = None).build()

            running(application) {
              val request = FakeRequest(GET, onPageSubmitRoute(taxYear, businessId, NormalMode))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual standard.routes.JourneyRecoveryController.onPageLoad().url
            }
          }

          "must redirect to Journey Recovery for a POST if no existing data is found" in {

            val application = applicationBuilder(userAnswers = None).build()

            running(application) {
              val request =
                FakeRequest(POST, onPageSubmitRoute(taxYear, businessId, NormalMode))
                  .withFormUrlEncodedBody(("value", mileage.toString))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER

              redirectLocation(result).value mustEqual standard.routes.JourneyRecoveryController.onPageLoad().url
            }
          }
        }
      }
    }
  }
}
