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
import models.NormalMode
import models.common.UserType
import models.database.UserAnswers
import navigation.{FakeTravelAndAccommodationNavigator, TravelAndAccommodationNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.travelAndAccommodation.{DisallowableTransportAndAccommodationPage, PublicTransportAndAccommodationExpensesPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import utils.MoneyUtils.formatMoney
import views.html.journeys.expenses.travelAndAccommodation.DisallowableTransportAndAccommodationView

import scala.concurrent.Future

class DisallowableTransportAndAccommodationControllerSpec extends SpecBase with MockitoSugar {

  val formProvider      = new CurrencyFormProvider()
  def onwardRoute: Call = Call("GET", "/foo")

  val validAnswer: BigDecimal = 35
  val expenses: BigDecimal    = 50
  val strExpense: String      = formatMoney(expenses)
  val form: UserType => Form[BigDecimal] = (userType: UserType) =>
    formProvider(
      DisallowableTransportAndAccommodationPage,
      userType,
      maxValue = expenses,
      prefix = Some("disallowableTransportAndAccommodation"),
      args = Seq(expenses.toString())
    )

  lazy val disallowableTransportAndAccommodationRoute: String =
    routes.DisallowableTransportAndAccommodationController.onPageLoad(taxYear, businessId, NormalMode).url

  "DisallowableTransportAndAccommodation Controller" - {
    UserType.values.foreach { userType =>
      s"when user is $userType" - {
        "must return OK and the correct view for a GET" in {
          val userAnswers = emptyUserAnswers
            .set(PublicTransportAndAccommodationExpensesPage, expenses, Some(businessId))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers), userType).build()

          running(application) {
            val request = FakeRequest(GET, disallowableTransportAndAccommodationRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[DisallowableTransportAndAccommodationView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form(userType), NormalMode, userType, taxYear, businessId, strExpense)(
              request,
              messages(application)).toString
          }
        }

        "redirect to 'there is a problem' page when data is missing for the page 'TODO'" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userType).build()

          running(application) {
            val request = FakeRequest(GET, disallowableTransportAndAccommodationRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must populate the view correctly on a GET when the question has previously been answered" in {

          val userAnswers = UserAnswers(userAnswersId)
            .set(PublicTransportAndAccommodationExpensesPage, expenses, Some(businessId))
            .success
            .value
            .set(DisallowableTransportAndAccommodationPage, validAnswer, Some(businessId))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers), userType).build()

          running(application) {
            val request = FakeRequest(GET, disallowableTransportAndAccommodationRoute)

            val view = application.injector.instanceOf[DisallowableTransportAndAccommodationView]

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form(userType).fill(validAnswer), NormalMode, userType, taxYear, businessId, strExpense)(
              request,
              messages(application)).toString
          }
        }

        "must redirect to the next page when valid data is submitted" in {

          val userAnswers = UserAnswers(userAnswersId)
            .set(PublicTransportAndAccommodationExpensesPage, expenses, Some(businessId))
            .success
            .value
          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(userAnswers), userType)
              .overrides(
                bind[TravelAndAccommodationNavigator].toInstance(new FakeTravelAndAccommodationNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, disallowableTransportAndAccommodationRoute)
                .withFormUrlEncodedBody(("value", validAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "must return a Bad Request and errors when invalid data is submitted" in {
          val userAnswers = UserAnswers(userAnswersId)
            .set(PublicTransportAndAccommodationExpensesPage, expenses, Some(businessId))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers), userType).build()

          running(application) {
            val request =
              FakeRequest(POST, disallowableTransportAndAccommodationRoute)
                .withFormUrlEncodedBody(("value", "invalid value"))

            val boundForm = form(userType).bind(Map("value" -> "invalid value"))

            val view = application.injector.instanceOf[DisallowableTransportAndAccommodationView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, NormalMode, userType, taxYear, businessId, strExpense)(
              request,
              messages(application)).toString
          }
        }
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, disallowableTransportAndAccommodationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, disallowableTransportAndAccommodationRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
