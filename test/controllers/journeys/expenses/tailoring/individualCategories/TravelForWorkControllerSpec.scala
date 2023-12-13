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

package controllers.journeys.expenses.tailoring.individualCategories

import base.SpecBase
import forms.expenses.tailoring.individualCategories.TravelForWorkFormProvider
import models.NormalMode
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.TaxiMinicabOrRoadHaulage.Yes
import models.journeys.expenses.individualCategories.TravelForWork
import navigation.{ExpensesTailoringNavigator, FakeExpensesTailoringNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.tailoring.individualCategories.{TaxiMinicabOrRoadHaulagePage, TravelForWorkPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.journeys.expenses.tailoring.individualCategories.TravelForWorkView

import scala.concurrent.Future

class TravelForWorkControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val travelForWorkRoute =
    controllers.journeys.expenses.tailoring.individualCategories.routes.TravelForWorkController.onPageLoad(taxYear, businessId, NormalMode).url

  val formProvider = new TravelForWorkFormProvider()
  val taxiDriver   = false

  case class UserScenario(isAgent: Boolean, form: Form[TravelForWork])

  val userScenarios = Seq(
    UserScenario(isAgent = false, formProvider(individual)),
    UserScenario(isAgent = true, formProvider(agent))
  )

  "TravelForWork Controller" - {

    "onPageLoad" - {
      userScenarios.foreach { userScenario =>
        s"when user is an ${userType(userScenario.isAgent)}" - {

          "must return OK and the correct view for a GET" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent)
              .build()

            running(application) {

              val request = FakeRequest(GET, travelForWorkRoute)

              val view = application.injector.instanceOf[TravelForWorkView]

              val result = route(application, request).value

              val expectedResult =
                view(userScenario.form, NormalMode, userType(userScenario.isAgent), taxYear, businessId, taxiDriver)(
                  request,
                  messages(application)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers = UserAnswers(userAnswersId).set(TravelForWorkPage, TravelForWork.values.head, Some(businessId)).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = userScenario.isAgent).build()

            running(application) {

              val request = FakeRequest(GET, travelForWorkRoute)

              val view = application.injector.instanceOf[TravelForWorkView]

              val result = route(application, request).value

              val expectedResult =
                view(userScenario.form.fill(TravelForWork.values.head), NormalMode, userType(userScenario.isAgent), taxYear, businessId, taxiDriver)(
                  request,
                  messages(application)).toString
              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }
        }
      }

      "must return OK and the correct view for a GET when user is taxi driver" in {
        val taxiDriver  = true
        val userAnswers = UserAnswers(userAnswersId).set(TaxiMinicabOrRoadHaulagePage, Yes, Some(businessId)).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {

          val request = FakeRequest(GET, travelForWorkRoute)

          val view = application.injector.instanceOf[TravelForWorkView]

          val result = route(application, request).value

          val expectedResult =
            view(formProvider(individual), NormalMode, userType(false), taxYear, businessId, taxiDriver)(request, messages(application)).toString

          status(result) mustEqual OK
          contentAsString(result) mustEqual expectedResult
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, travelForWorkRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {
      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[ExpensesTailoringNavigator].toInstance(new FakeExpensesTailoringNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, travelForWorkRoute)
              .withFormUrlEncodedBody(("value", TravelForWork.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      userScenarios.foreach { userScenario =>
        s"when user is an ${userType(userScenario.isAgent)}" - {

          "must return a Bad Request and errors when empty data is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent).build()

            running(application) {
              val request =
                FakeRequest(POST, travelForWorkRoute)
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = userScenario.form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[TravelForWorkView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userType(userScenario.isAgent), taxYear, businessId, taxiDriver)(request, messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must return a Bad Request and errors when invalid data is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent).build()

            running(application) {
              val request =
                FakeRequest(POST, travelForWorkRoute)
                  .withFormUrlEncodedBody(("value", "invalid value"))

              val boundForm = userScenario.form.bind(Map("value" -> "invalid value"))

              val view = application.injector.instanceOf[TravelForWorkView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userType(userScenario.isAgent), taxYear, businessId, taxiDriver)(request, messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }

          "redirect to Journey Recovery for a POST if no existing data is found" in {

            val application = applicationBuilder(userAnswers = None).build()

            running(application) {
              val request =
                FakeRequest(POST, travelForWorkRoute)
                  .withFormUrlEncodedBody(("value", TravelForWork.values.head.toString))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER

              redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
            }
          }
        }
      }
    }
  }
}