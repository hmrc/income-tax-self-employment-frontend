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
import forms.expenses.travelAndAccommodation.YourFlatRateForVehicleExpensesFormProvider
import models.NormalMode
import models.common.UserType
import models.database.UserAnswers
import models.journeys.expenses.travelAndAccommodation.YourFlatRateForVehicleExpenses
import navigation.{FakeTravelAndAccommodationNavigator, TravelAndAccommodationNavigator}
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.mockito.matchers.MacroBasedMatchers
import pages.expenses.travelAndAccommodation._
import play.api.data.Form
import play.api.http.Status.SEE_OTHER
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import utils.MoneyUtils.formatMoney
import viewmodels.checkAnswers.expenses.travelAndAccommodation.TravelMileageSummaryViewModel
import views.html.journeys.expenses.travelAndAccommodation.YourFlatRateForVehicleExpensesView

import scala.concurrent.Future

class YourFlatRateForVehicleExpensesControllerSpec extends SpecBase with MacroBasedMatchers {

  def onwardRoute: Call = Call("GET", "/foo")

  lazy val yourFlatRateForVehicleExpensesRoute: String =
    routes.YourFlatRateForVehicleExpensesController.onPageLoad(taxYear, businessId, NormalMode).url
  val workMileage: String   = "90"
  val mileage: BigDecimal   = BigDecimal(workMileage)
  val totalFlatRate: String = formatMoney(TravelMileageSummaryViewModel.totalFlatRateExpense(mileage))
  val formProvider          = new YourFlatRateForVehicleExpensesFormProvider()

  "YourFlatRateForVehicleExpenses Controller" - {

    Seq(UserType.Individual, UserType.Agent).foreach { userType =>
      s"when user is $userType" - {
        val form: Form[YourFlatRateForVehicleExpenses] = formProvider(mileage, userType)
        "must return OK and the correct view for a GET" in {

          val userAnswers = emptyUserAnswers
            .set(SimplifiedExpensesPage, true, Some(businessId))
            .success
            .value
            .set(TravelForWorkYourMileagePage, mileage, Some(businessId))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers), userType).build()

          val summaryList: SummaryList = TravelMileageSummaryViewModel.buildSummaryList(mileage)(messages(application))

          val request = FakeRequest(GET, yourFlatRateForVehicleExpensesRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[YourFlatRateForVehicleExpensesView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            form,
            taxYear,
            businessId,
            userType,
            workMileage,
            totalFlatRate,
            summaryList,
            showSelection = false,
            NormalMode)(request, messages(application)).toString
          application.stop()
        }

        "redirect to Journey Recovery for a GET if no existing data is found for TravelForWorkYourMileagePage" in {

          val userAnswers = emptyUserAnswers
            .set(SimplifiedExpensesPage, true, Some(businessId))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers), userType).build()

          val request = FakeRequest(GET, yourFlatRateForVehicleExpensesRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
          application.stop()
        }

        "must populate the view correctly on a GET when the question has previously been answered" in {

          val userAnswers =
            UserAnswers(userAnswersId)
              .set(SimplifiedExpensesPage, true, Some(businessId))
              .success
              .value
              .set(YourFlatRateForVehicleExpensesPage, YourFlatRateForVehicleExpenses.values.head)
              .success
              .value
              .set(TravelForWorkYourMileagePage, mileage, Some(businessId))
              .success
              .value

          val application = applicationBuilder(userAnswers = Some(userAnswers), userType).build()

          val summaryList: SummaryList = TravelMileageSummaryViewModel.buildSummaryList(mileage)(messages(application))

          val request = FakeRequest(GET, yourFlatRateForVehicleExpensesRoute)

          val view = application.injector.instanceOf[YourFlatRateForVehicleExpensesView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            form.fill(YourFlatRateForVehicleExpenses.values.head),
            taxYear,
            businessId,
            userType,
            workMileage,
            totalFlatRate,
            summaryList,
            showSelection = false,
            NormalMode
          )(request, messages(application)).toString
          application.stop()
        }

        "must redirect to the next page when valid data is submitted" in {

          val userAnswers =
            UserAnswers(userAnswersId)
              .set(SimplifiedExpensesPage, true, Some(businessId))
              .success
              .value
              .set(TravelForWorkYourMileagePage, mileage, Some(businessId))
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

          val request =
            FakeRequest(POST, yourFlatRateForVehicleExpensesRoute)
              .withFormUrlEncodedBody(("value", YourFlatRateForVehicleExpenses.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
          application.stop()
        }

        "redirect to Journey Recovery for a POST if no existing data is found for TravelForWorkYourMileagePage" in {

          val application = applicationBuilder(userAnswers = None, userType).build()

          val request = FakeRequest(POST, yourFlatRateForVehicleExpensesRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
          application.stop()
        }

        "must return a Bad Request and errors when invalid data is submitted" in {

          val userAnswers =
            UserAnswers(userAnswersId)
              .set(SimplifiedExpensesPage, false, Some(businessId))
              .success
              .value
              .set(TravelForWorkYourMileagePage, mileage, Some(businessId))
              .success
              .value

          val application = applicationBuilder(userAnswers = Some(userAnswers), userType).build()

          val summaryList: SummaryList = TravelMileageSummaryViewModel.buildSummaryList(mileage)(messages(application))

          val request =
            FakeRequest(POST, yourFlatRateForVehicleExpensesRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))

          val boundForm = form.bind(Map("value" -> "invalid value"))

          val view = application.injector.instanceOf[YourFlatRateForVehicleExpensesView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(
            boundForm,
            taxYear,
            businessId,
            userType,
            workMileage,
            totalFlatRate,
            summaryList,
            showSelection = true,
            NormalMode)(request, messages(application)).toString
          application.stop()
        }
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, yourFlatRateForVehicleExpensesRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
      application.stop()
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, yourFlatRateForVehicleExpensesRoute)
          .withFormUrlEncodedBody(("value", YourFlatRateForVehicleExpenses.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
      application.stop()
    }
  }
}
