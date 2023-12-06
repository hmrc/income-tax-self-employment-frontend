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

package controllers.journeys.income

import base.SpecBase
import controllers.journeys.income.routes.{IncomeCYAController, NonTurnoverIncomeAmountController, TurnoverIncomeAmountController}
import controllers.standard.routes.JourneyRecoveryController
import forms.income.NonTurnoverIncomeAmountFormProvider
import models.common.UserType
import models.database.UserAnswers
import models.{CheckMode, NormalMode}
import navigation.{FakeIncomeNavigator, IncomeNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.income.NonTurnoverIncomeAmountPage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.journeys.income.NonTurnoverIncomeAmountView

import scala.concurrent.Future

class NonTurnoverIncomeAmountControllerSpec extends SpecBase with MockitoSugar {

  val formProvider            = new NonTurnoverIncomeAmountFormProvider()
  val validAnswer: BigDecimal = 100
  val onwardRoute             = TurnoverIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode)
  val cyaCall                 = IncomeCYAController.onPageLoad(taxYear, businessId)

  case class UserScenario(authUserType: UserType, form: Form[BigDecimal])

  val userScenarios = Seq(
    UserScenario(authUserType = UserType.Individual, formProvider(UserType.Individual)),
    UserScenario(authUserType = UserType.Agent, formProvider(UserType.Agent))
  )

  "NonTurnoverIncomeAmount Controller" - {

    "onPageLoad" - {

      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.authUserType}" - {
          "must return OK and the correct view for a GET" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userScenario.authUserType).build()

            running(application) {
              val request = FakeRequest(GET, NonTurnoverIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[NonTurnoverIncomeAmountView]

              val expectedResult =
                view(userScenario.form, NormalMode, userScenario.authUserType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers = UserAnswers(userAnswersId).set(NonTurnoverIncomeAmountPage, validAnswer, Some(businessId)).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers), userScenario.authUserType).build()

            running(application) {
              val request = FakeRequest(GET, NonTurnoverIncomeAmountController.onPageLoad(taxYear, businessId, CheckMode).url)

              val view = application.injector.instanceOf[NonTurnoverIncomeAmountView]

              val result = route(application, request).value

              val expectedResult = view(userScenario.form.fill(validAnswer), CheckMode, userScenario.authUserType, taxYear, businessId)(
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
          val request = FakeRequest(GET, NonTurnoverIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "when valid data is submitted must redirect to the" - {
        "Turnover Income Amount page when in NormalMode" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, NonTurnoverIncomeAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                .withFormUrlEncodedBody(("value", validAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "CYA page when in CheckMode and income model is now completed" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(cyaCall)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, NonTurnoverIncomeAmountController.onSubmit(taxYear, businessId, CheckMode).url)
                .withFormUrlEncodedBody(("value", validAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual cyaCall.url
          }
        }
      }

      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.authUserType}" - {
          "must return a Bad Request and errors when an empty form is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userType = userScenario.authUserType).build()

            running(application) {
              val request =
                FakeRequest(POST, NonTurnoverIncomeAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = userScenario.form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[NonTurnoverIncomeAmountView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.authUserType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must return a Bad Request and errors when invalid data is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userType = userScenario.authUserType).build()

            running(application) {
              val request =
                FakeRequest(POST, NonTurnoverIncomeAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", "non-BigDecimal"))

              val boundForm = userScenario.form.bind(Map("value" -> "non-BigDecimal"))

              val view = application.injector.instanceOf[NonTurnoverIncomeAmountView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.authUserType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }

          "a negative number is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userType = userScenario.authUserType).build()

            running(application) {
              val request =
                FakeRequest(POST, NonTurnoverIncomeAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", "-23"))

              val boundForm = userScenario.form.bind(Map("value" -> "-23"))

              val view = application.injector.instanceOf[NonTurnoverIncomeAmountView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.authUserType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }

          "turnover income amount exceeds £100,000,000,000.00" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userType = userScenario.authUserType).build()

            running(application) {
              val request =
                FakeRequest(POST, NonTurnoverIncomeAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", "100000000000.01"))

              val boundForm = userScenario.form.bind(Map("value" -> "100000000000.01"))

              val view = application.injector.instanceOf[NonTurnoverIncomeAmountView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.authUserType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, NonTurnoverIncomeAmountController.onSubmit(taxYear, businessId, NormalMode).url)
              .withFormUrlEncodedBody(("value", validAnswer.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

}
