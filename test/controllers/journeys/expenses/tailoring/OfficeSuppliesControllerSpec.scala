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

package controllers.journeys.expenses.tailoring

import base.SpecBase
import controllers.journeys.expenses.tailoring.routes.OfficeSuppliesController
import controllers.standard.routes.JourneyRecoveryController
import forms.expenses.tailoring.OfficeSuppliesFormProvider
import models.NormalMode
import models.database.UserAnswers
import models.journeys.expenses.OfficeSupplies
import navigation.{ExpensesTailoringNavigator, FakeExpensesTailoringNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.tailoring.OfficeSuppliesPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SelfEmploymentService
import views.html.journeys.expenses.tailoring.OfficeSuppliesView

import scala.concurrent.Future

class OfficeSuppliesControllerSpec extends SpecBase with MockitoSugar {

  lazy val officeSuppliesRoute = OfficeSuppliesController.onPageLoad(taxYear, businessId, NormalMode).url

  val formProvider = new OfficeSuppliesFormProvider()

  val mockService: SelfEmploymentService = mock[SelfEmploymentService]

  val userScenarios = Seq(
    UserScenario(isAgent = false, formProvider(individual), accrual),
    UserScenario(isAgent = true, formProvider(agent), cash)
  )

  def onwardRoute = Call("GET", "/foo")

  case class UserScenario(isAgent: Boolean, form: Form[OfficeSupplies], accountingType: String)

  "OfficeSupplies Controller" - {

    "onPageLoad" - {
      userScenarios.foreach { userScenario =>
        s"when user is an ${userType(userScenario.isAgent)} and using ${userScenario.accountingType} accounting type" - {
          "must return OK and the correct view for a GET" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {
              when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(userScenario.accountingType))

              val request = FakeRequest(GET, officeSuppliesRoute)

              val view = application.injector.instanceOf[OfficeSuppliesView]

              val result = route(application, request).value

              val expectedResult =
                view(userScenario.form, NormalMode, userType(userScenario.isAgent), taxYear, businessId, userScenario.accountingType)(
                  request,
                  messages(application)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers =
              UserAnswers(userAnswersId).set(OfficeSuppliesPage, OfficeSupplies.values.head, Some(businessId)).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = userScenario.isAgent)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {
              when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(userScenario.accountingType))

              val request = FakeRequest(GET, officeSuppliesRoute)

              val view = application.injector.instanceOf[OfficeSuppliesView]

              val result = route(application, request).value

              val expectedResult =
                view(
                  userScenario.form.fill(OfficeSupplies.values.head),
                  NormalMode,
                  userType(userScenario.isAgent),
                  taxYear,
                  businessId,
                  userScenario.accountingType
                )(request, messages(application)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, officeSuppliesRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
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
              bind[SelfEmploymentService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(accrual))

          val request =
            FakeRequest(POST, officeSuppliesRoute)
              .withFormUrlEncodedBody(("value", OfficeSupplies.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      userScenarios.foreach { userScenario =>
        s"when user is an ${userType(userScenario.isAgent)} and using ${userScenario.accountingType} accounting type" - {
          "must return a Bad Request and errors when empty form is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {
              when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(userScenario.accountingType))

              val request =
                FakeRequest(POST, officeSuppliesRoute)
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = userScenario.form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[OfficeSuppliesView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userType(userScenario.isAgent), taxYear, businessId, userScenario.accountingType)(
                  request,
                  messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must return a Bad Request and errors when invalid data is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {
              when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(userScenario.accountingType))

              val request =
                FakeRequest(POST, officeSuppliesRoute)
                  .withFormUrlEncodedBody(("value", "invalid"))

              val boundForm = userScenario.form.bind(Map("value" -> "invalid"))

              val view = application.injector.instanceOf[OfficeSuppliesView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userType(userScenario.isAgent), taxYear, businessId, userScenario.accountingType)(
                  request,
                  messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }

          "redirect to Journey Recovery for a POST if no existing data is found" in {

            val application = applicationBuilder(userAnswers = None).build()

            running(application) {
              val request =
                FakeRequest(POST, officeSuppliesRoute)
                  .withFormUrlEncodedBody(("value", OfficeSupplies.values.head.toString))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER

              redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
            }
          }
        }
      }
    }
  }
}
