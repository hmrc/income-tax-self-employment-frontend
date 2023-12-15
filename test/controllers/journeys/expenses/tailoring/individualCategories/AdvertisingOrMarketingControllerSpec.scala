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
import forms.expenses.tailoring.individualCategories.AdvertisingOrMarketingFormProvider
import models.NormalMode
import models.common.UserType
import models.common.UserType.{Agent, Individual}
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.AdvertisingOrMarketing
import navigation.{ExpensesTailoringNavigator, FakeExpensesTailoringNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.tailoring.individualCategories.AdvertisingOrMarketingPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SelfEmploymentService
import views.html.journeys.expenses.tailoring.individualCategories.AdvertisingOrMarketingView

import scala.concurrent.Future

class AdvertisingOrMarketingControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val advertisingOrMarketingRoute =
    controllers.journeys.expenses.tailoring.individualCategories.routes.AdvertisingOrMarketingController
      .onPageLoad(taxYear, businessId, NormalMode)
      .url

  val mockSessionRepository: SessionRepository = mock[SessionRepository]
  val mockService: SelfEmploymentService       = mock[SelfEmploymentService]

  val formProvider = new AdvertisingOrMarketingFormProvider()

  case class UserScenario(userType: UserType, form: Form[AdvertisingOrMarketing])

  val userScenarios = Seq(
    UserScenario(userType = Individual, formProvider(Individual)),
    UserScenario(userType = Agent, formProvider(Agent))
  )

  "AdvertisingOrMarketing Controller" - {

    "onPageLoad" - {
      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.userType}" - {

          "must return OK and the correct view for a GET" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userScenario.userType)
              .build()

            running(application) {

              val request = FakeRequest(GET, advertisingOrMarketingRoute)

              val view = application.injector.instanceOf[AdvertisingOrMarketingView]

              val result = route(application, request).value

              val expectedResult =
                view(userScenario.form, NormalMode, userScenario.userType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers =
              UserAnswers(userAnswersId).set(AdvertisingOrMarketingPage, AdvertisingOrMarketing.values.head, Some(businessId)).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers), userScenario.userType).build()

            running(application) {
              val request = FakeRequest(GET, advertisingOrMarketingRoute)

              val view = application.injector.instanceOf[AdvertisingOrMarketingView]

              val result = route(application, request).value

              val expectedResult =
                view(userScenario.form.fill(AdvertisingOrMarketing.values.head), NormalMode, userScenario.userType, taxYear, businessId)(
                  request,
                  messages(application)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, advertisingOrMarketingRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must redirect to the EntertainmentCostsPage when valid data is submitted and accounting type is 'ACCRUAL'" in {

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[ExpensesTailoringNavigator].toInstance(new FakeExpensesTailoringNavigator(onwardRoute)),
              bind[SelfEmploymentService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(accrual))

          val request =
            FakeRequest(POST, advertisingOrMarketingRoute)
              .withFormUrlEncodedBody(("value", AdvertisingOrMarketing.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }
      "must redirect to the ProfessionalServiceExpensesPage when valid data is submitted and accounting type is 'CASH'" in {

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[ExpensesTailoringNavigator].toInstance(new FakeExpensesTailoringNavigator(onwardRoute)),
              bind[SelfEmploymentService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(cash))

          val request =
            FakeRequest(POST, advertisingOrMarketingRoute)
              .withFormUrlEncodedBody(("value", AdvertisingOrMarketing.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.userType}" - {
          "must return a Bad Request and errors when empty data is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userScenario.userType)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {
              when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(accrual))

              val request =
                FakeRequest(POST, advertisingOrMarketingRoute)
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = userScenario.form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[AdvertisingOrMarketingView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.userType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must return a Bad Request and errors when invalid data is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userScenario.userType)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {
              when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(accrual))

              val request =
                FakeRequest(POST, advertisingOrMarketingRoute)
                  .withFormUrlEncodedBody(("value", "invalid value"))

              val boundForm = userScenario.form.bind(Map("value" -> "invalid value"))

              val view = application.injector.instanceOf[AdvertisingOrMarketingView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.userType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }
        }
      }

      "redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, advertisingOrMarketingRoute)
              .withFormUrlEncodedBody(("value", AdvertisingOrMarketing.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

}
