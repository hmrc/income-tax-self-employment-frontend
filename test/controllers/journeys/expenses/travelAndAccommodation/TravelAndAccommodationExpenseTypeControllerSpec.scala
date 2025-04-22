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
import forms.expenses.travelAndAccommodation.TravelAndAccommodationFormProvider
import models.common.{BusinessId, TaxYear, UserType}
import models.journeys.expenses.travelAndAccommodation.TravelAndAccommodationExpenseType
import models.{Mode, NormalMode}
import navigation.{FakeTravelAndAccommodationNavigator, TravelAndAccommodationNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import play.api.test._
import repositories.SessionRepository
import views.html.journeys.expenses.travelAndAccommodation.TravelAndAccommodationExpenseTypeView

import scala.concurrent.Future

class TravelAndAccommodationExpenseTypeControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  def onwardRoute: Call = Call("GET", "/foo")

  private val mode = NormalMode

  val formProvider                                       = new TravelAndAccommodationFormProvider()
  val form: Form[Set[TravelAndAccommodationExpenseType]] = formProvider(UserType.Individual)

  case class UserScenario(userType: UserType, form: Form[Set[TravelAndAccommodationExpenseType]])

  val userScenarios: Seq[UserScenario] = Seq(
    UserScenario(userType = UserType.Individual, formProvider(UserType.Individual)),
    UserScenario(userType = UserType.Agent, formProvider(UserType.Agent))
  )

  private def onPageLoadRoute(taxYear: TaxYear, businessId: BusinessId, mode: Mode): String =
    controllers.journeys.expenses.travelAndAccommodation.routes.TravelAndAccommodationExpenseTypeController.onPageLoad(taxYear, businessId, mode).url

  private def onSubmitRoute(taxYear: TaxYear, businessId: BusinessId, mode: Mode): String =
    controllers.journeys.expenses.travelAndAccommodation.routes.TravelAndAccommodationExpenseTypeController.onSubmit(taxYear, businessId, mode).url

  "TravelAndAccommodationExpenseTypeController" - {
    "onPageLoad" - {
      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.userType}" - {

          "must return OK and the correct view for a GET" in {
            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userScenario.userType)
              .build()

            val request = FakeRequest(GET, onPageLoadRoute(taxYear, businessId, mode))

            val view = application.injector.instanceOf[TravelAndAccommodationExpenseTypeView]

            val result = route(application, request).value

            val expectedResult =
              view(userScenario.form, NormalMode, userScenario.userType, taxYear, businessId)(request, messages(application)).toString

            status(result) mustEqual OK
            contentAsString(result) mustEqual expectedResult
            application.stop()
          }

          "must redirect to Journey Recovery for a GET if no existing data is found" in {
            val application = applicationBuilder(userAnswers = None).build()

            val request = FakeRequest(GET, onPageLoadRoute(taxYear, businessId, mode))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
            application.stop()
          }
        }
      }
    }

    "onSubmit" - {
      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.userType}" - {
          "must redirect to the next page when valid data is submitted" in {
            val mockSessionRepository = mock[SessionRepository]

            when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

            val application =
              applicationBuilder(userAnswers = Some(emptyUserAnswers), userType = userScenario.userType)
                .overrides(
                  bind[TravelAndAccommodationNavigator].toInstance(new FakeTravelAndAccommodationNavigator(onwardRoute)),
                  bind[SessionRepository].toInstance(mockSessionRepository)
                )
                .build()

            val request =
              FakeRequest(POST, onSubmitRoute(taxYear, businessId, mode))
                .withFormUrlEncodedBody(("value[0]", TravelAndAccommodationExpenseType.values.head.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
            application.stop()
          }

          "must return a Bad Request and errors when invalid data is submitted" in {
            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userScenario.userType).build()

            val request =
              FakeRequest(POST, onSubmitRoute(taxYear, businessId, mode))
                .withFormUrlEncodedBody(("value[0]", "invalid value"))

            val boundForm = formProvider(userScenario.userType).bind(Map("value[0]" -> "invalid value"))

            val view = application.injector.instanceOf[TravelAndAccommodationExpenseTypeView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, mode, userScenario.userType, taxYear, businessId)(
              request,
              messages(application)).toString
            application.stop()
          }

          "must redirect to Journey Recovery for a POST if no existing data is found" in {
            val application = applicationBuilder(userAnswers = None).build()

            val request =
              FakeRequest(POST, onSubmitRoute(taxYear, businessId, mode))
                .withFormUrlEncodedBody(("value", TravelAndAccommodationExpenseType.values.head.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
            application.stop()
          }
        }
      }
    }

  }
}
